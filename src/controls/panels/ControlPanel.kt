package controls.panels

import controls.Control
import controls.panels.ListDirection.Col
import controls.panels.ListDirection.Row
import coordinate.BoundRect
import coordinate.PaddingRect
import coordinate.Point
import geomerativefork.src.util.mapArray
import kotlin.math.max

enum class ListDirection {
  Row,
  Col,
}

sealed class ControlPanel(
  val name: String = "",
  var style: ControlStyle = ControlStyle.NO_OVERRIDES,
  open val widthRatio: Double = 1.0,
  open val heightRatio: Double = 1.0,
  var parent: ControlPanel? = null
) : Panelable {
  fun isEmpty() = heightRatio == 0.0 || widthRatio == 0.0

  val styleFromParents: ControlStyle
    get() = parent?.let {
      return it.styleFromParents.withOverrides(style)
    } ?: style

  val id: String
    get() = (
      parent?.let { return it.id + name } ?: name
      ).replace(" ", "")

  override fun toControlPanel(): ControlPanel = this

  fun with(
    widthRatio: Double? = null,
    heightRatio: Double? = null,
    style: ControlStyle? = null
  ): ControlPanel = when (this) {
    is ControlTab -> ControlTab(
      this,
      style = style,
    )
    is ControlList -> ControlList(
      this,
      style = style,
      widthOverride = widthRatio,
      heightOverride = heightRatio,
    )
    is ControlItem -> ControlItem(
      this,
      style = style,
      widthRatio = widthRatio,
      heightRatio = heightRatio,
    )
  }


  var paddingOverrides: PaddingRect?
    get() = style.paddingOverrides
    set(value) {
      style = style.withPadding(value)
    }
}

/**
 * Contains data to build a tab full of controls.
 */
class ControlTab(
  name: String,
  style: ControlStyle = ControlStyle.NO_OVERRIDES,
  val panel: ControlPanel
) : ControlPanel(name, style) {
  init {
    panel.parent = this
  }

  constructor(
    name: String,
    style: ControlStyle = ControlStyle.NO_OVERRIDES,
    vararg rows: Panelable = arrayOf()
  ) : this(name, style, ControlList.col(*rows))

  constructor(
    tab: ControlTab,
    name: String? = null,
    style: ControlStyle? = ControlStyle.NO_OVERRIDES
  ) : this(
    name ?: tab.name,
    style ?: tab.style,
    tab.panel,
  )

  fun withName(newName: String) = ControlTab(this, newName)
  fun withName(nameFunc: (String) -> String) = ControlTab(this, nameFunc(name))

  companion object {
    fun tab(name: String, vararg sections: Panelable) = ControlTab(
      name,
      ControlStyle.NO_OVERRIDES,
      *sections.mapArray { it.toControlPanel() },
    )

    fun singleTab(name: String, vararg sections: Panelable) = listOf(
      ControlTab(
        name,
        ControlStyle.NO_OVERRIDES,
        *sections.mapArray { it.toControlPanel() },
      ),
    )

    fun layerTab(vararg sections: Panelable) = listOf(
      tab("L", *sections),
    )
  }
}

class ControlList(
  name: String = "",
  style: ControlStyle = ControlStyle.NO_OVERRIDES,
  val direction: ListDirection = Row,
  val widthOverride: Double? = null,
  val heightOverride: Double? = null,
  val items: List<ControlPanel>,
) : ControlPanel(name, style) {
  init {
    items.forEach { it.parent = this }
  }

  constructor(
    name: String = "",
    style: ControlStyle = ControlStyle.NO_OVERRIDES,
    direction: ListDirection = Row,
    widthOverride: Double? = null,
    heightOverride: Double? = null,
    vararg items: Panelable = arrayOf(),
  ) : this(
    name,
    style,
    direction,
    widthOverride,
    heightOverride,
    items.map { it.toControlPanel() }.toList(),
  )

  constructor(
    list: ControlList,
    name: String? = null,
    style: ControlStyle? = null,
    direction: ListDirection? = null,
    widthOverride: Double? = null,
    heightOverride: Double? = null,
  ) : this(
    name ?: list.name,
    style ?: list.style,
    direction ?: list.direction,
    widthOverride ?: list.widthOverride,
    heightOverride ?: list.heightOverride,
    list.items,
  )

  fun childBounds(totalBound: BoundRect): List<Pair<ControlPanel, BoundRect>> {
    val totalBoundMinusPadding = totalBound.minusPadding(style.padding)
    var currPoint = totalBoundMinusPadding.topLeft
    val isRow = direction == Row

    fun getTotalPadding(numItems: Int, paddingBetweenItems: Double) =
      max(0.0, (numItems - 1) * paddingBetweenItems)

    val numItems: Point = if (isRow) Point(items.size, 0) else Point(0, items.size)
    val paddingBetweenElements =
      Point(style.childPadding.totalHorizontal(), style.childPadding.totalVertical())

    val paddingBetweenItemsTotal = Point.zip(numItems, paddingBetweenElements) { num, padding ->
      getTotalPadding(num.toInt(), padding)
    }

    val usablePanelSize = totalBoundMinusPadding.size - paddingBetweenItemsTotal

    return items.map { item ->
      val elementSize = usablePanelSize * (
        if (isRow) Point(item.widthRatio / widthRatio, 1)
        else Point(1, item.heightRatio / heightRatio)
        )
      val childBound = BoundRect(currPoint, elementSize)

      currPoint =
        if (isRow) childBound.topRight + paddingBetweenElements.zeroY()
        else childBound.bottomLeft + paddingBetweenElements.zeroX()

      item to childBound
    }
  }

  override
  val heightRatio by lazy {
    val ratio = when (direction) {
      Row -> items.maxOfOrNull { it.heightRatio }
        ?: 0.0
      Col -> items.sumByDouble { it.heightRatio }
    }

    if (ratio == 0.0)
      ratio
    else heightOverride ?: ratio
  }

  override
  val widthRatio by lazy {
    val ratio = when (direction) {
      Row -> items.sumByDouble { it.widthRatio }
      Col -> items.maxOfOrNull { it.widthRatio }
        ?: 0.0
    }

    if (ratio == 0.0)
      ratio
    else widthOverride ?: ratio
  }

  companion object {
    fun rowIf(
      predicate: Boolean,
      vararg items: Panelable?,
      widthOverride: Double? = null,
      heightOverride: Double? = null,
    ) = if (predicate) row(
      *items,
      widthOverride = widthOverride,
      heightOverride = heightOverride,
    ) else null

    fun colIf(
      predicate: Boolean,
      vararg items: Panelable?,
      widthOverride: Double? = null,
      heightOverride: Double? = null,
    ) = if (predicate) col(
      *items,
      widthOverride = widthOverride,
      heightOverride = heightOverride,
    ) else null

    fun row(
      name: String,
      vararg items: Panelable? = arrayOf(),
      widthOverride: Number? = null,
      heightOverride: Number? = null,
    ) = ControlList(
      name = name,
      direction = Row,
      widthOverride = widthOverride?.toDouble(),
      heightOverride = heightOverride?.toDouble(),
      items = items.filterNotNull().toTypedArray(),
    )

    fun row(
      vararg items: Panelable? = arrayOf(),
      widthOverride: Number? = null,
      heightOverride: Number? = null,
    ) = row(
      "", *items,
      widthOverride = widthOverride,
      heightOverride = heightOverride,
    )

    fun col(
      vararg items: Panelable? = arrayOf(),
      widthOverride: Number? = null,
      heightOverride: Number? = null,
    ) = col(
      "",
      *items,
      widthOverride = widthOverride,
      heightOverride = heightOverride,
    )

    fun col(
      name: String = "",
      vararg items: Panelable? = arrayOf(),
      widthOverride: Number? = null,
      heightOverride: Number? = null,
    ) = ControlList(
      name = name,
      direction = Col,
      widthOverride = widthOverride?.toDouble(),
      heightOverride = heightOverride?.toDouble(),
      items = items.filterNotNull().toTypedArray(),
    )
  }
}

class ControlItem(
  name: String = "",
  style: ControlStyle = ControlStyle.NO_OVERRIDES,
  widthRatio: Double = 1.0,
  heightRatio: Double = 1.0,
  val control: Control<*>,
) : ControlPanel(name, style, widthRatio, heightRatio) {
  override fun toControlPanel(): ControlPanel = this

  constructor(
    item: ControlItem,
    name: String? = null,
    style: ControlStyle? = null,
    widthRatio: Number? = null,
    heightRatio: Number? = null
  ) :
    this(
      name ?: item.name,
      style ?: item.style,
      widthRatio?.toDouble() ?: item.widthRatio,
      heightRatio?.toDouble() ?: item.heightRatio,
      item.control,
    )
}
