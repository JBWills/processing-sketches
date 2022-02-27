package controls.panels

import BaseSketch
import controlP5.ControlP5
import controls.controlsealedclasses.Control
import controls.panels.ControlList.Companion.col
import controls.panels.ControlStyle.Companion.EmptyStyle
import controls.panels.ListDirection.Col
import controls.panels.ListDirection.Row
import coordinate.BoundRect
import coordinate.Point
import kotlin.math.max

enum class ListDirection {
  Row,
  Col,
}

sealed class ControlPanel(
  val name: String = "",
  var style: ControlStyle = EmptyStyle,
  open val widthRatio: Double = 1.0,
  open val heightRatio: Double = 1.0,
  var parent: ControlPanel? = null,
) : Panelable {
  init {
    incrementedId++
  }

  fun isEmpty() = heightRatio == 0.0 || widthRatio == 0.0

  val styleFromParents: ControlStyle
    get() = parent?.let {
      return it.styleFromParents.withOverrides(style)
    } ?: style

  private val nameWithIncrementedId: String = "$name($incrementedId)"

  val id: String
    get() = (
      parent?.let { return "${it.id} > $nameWithIncrementedId" } ?: nameWithIncrementedId
      )

  override fun toControlPanel(): ControlPanel = this

  fun overrideStyles(styleOverrides: ControlStyle): ControlPanel {
    style = style.withOverrides(styleOverrides)
    return this
  }

  fun with(
    widthRatio: Double? = null,
    heightRatio: Double? = null,
    style: ControlStyle? = null,
    tabStyle: TabStyle? = null,
  ): ControlPanel = when (this) {
    is ControlTab -> ControlTab(
      this,
      tabStyle = tabStyle ?: style?.let { TabStyle(it) },
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

  override fun toString(): String {
    return "ControlPanel(name='$name', parent=${parent?.name})"
  }

  companion object {
    var incrementedId = 0
  }
}

/**
 * Contains data to build a tab full of controls. Note that a tab cannot contain other controls, it
 *   can only contain [ControlList]s or [ControlItem]s.
 *
 * @property panel the root panel of the [ControlTab]
 *
 * @param name the name of the tab, this will display at the top of the controlFrame and should be
 *   unique compared to other tabs, since it will be used to generate view IDs.
 * @param tabStyle the style to apply to the panels and controls in the tab.
 */
class ControlTab(
  name: String,
  val tabStyle: TabStyle = TabStyle.Base,
  val panel: ControlPanel
) : ControlPanel(name, tabStyle.nonNullControlStyle) {
  init {
    panel.parent = this

    if (name == "global") {
      throw Exception("Can name a tab \"global\". I know, it's annoying. You can do \"Global\" instead.")
    }
  }

  @Deprecated(
    "Use withStyle(TabStyle) instead.",
    ReplaceWith("withStyle(tabStyle)", "controls.panels.ControlTab"),
  )
  override fun withStyle(style: ControlStyle?): ControlPanel {
    return ControlTab(name, tabStyle.copy(controlStyle = style), panel)
  }

  fun withStyle(style: TabStyle): ControlTab {
    return ControlTab(name, style, panel)
  }

  constructor(
    name: String,
    tabStyle: TabStyle = TabStyle.Base,
    vararg rows: Panelable = arrayOf()
  ) : this(name, tabStyle, col { +rows.toList() }.toControlPanel())

  constructor(
    tab: ControlTab,
    name: String? = null,
    tabStyle: TabStyle? = null,
  ) : this(
    name ?: tab.name,
    tabStyle ?: tab.tabStyle,
    tab.panel,
  )

  fun withName(newName: String) = ControlTab(this, newName)

  override fun toString(): String {
    return "ControlTab(name = $name, panel=$panel, base=${super.toString()})"
  }

  companion object {
    fun tab(name: String, style: TabStyle? = null, block: PanelBuilder.() -> Unit) =
      ControlTab(name, style ?: TabStyle.Base, col(block = block))
  }
}

/**
 * Panel that renders a list of [ControlItem]s, either horizontally or vertically.
 *
 * @property direction [ListDirection.Col] for a vertical list, [ListDirection.Row] for a horizontal
 *   list.
 * @property widthOverride if set, the list will have the width ratio specified, if not set, the
 *   width will be set via children values.
 * @property heightOverride if set, the list will have the height ratio specified, if not set, the
 *   height will be set via children values.
 * @property items a list of [ControlPanel]s. These can be single controls or control lists
 *
 * @param name the name of the [ControlPanel]
 * @param style the style to apply to the panel and all of its children.
 */
class ControlList(
  name: String = "",
  style: ControlStyle = EmptyStyle,
  val direction: ListDirection = Row,
  private val widthOverride: Double? = null,
  private val heightOverride: Double? = null,
  private val items: List<ControlPanel>,
) : ControlPanel(name, style) {
  init {
    items.forEach { it.parent = this }
  }

  constructor(
    name: String = "",
    style: ControlStyle = EmptyStyle,
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
      val childBound = BoundRect(currPoint, elementSize.x, elementSize.y)

      currPoint =
        if (isRow) childBound.topRight + paddingBetweenElements.zeroY()
        else childBound.bottomLeft + paddingBetweenElements.zeroX()

      item to childBound
    }
  }

  override fun toString(): String {
    return "ControlList(direction=$direction, widthOverride=$widthOverride, heightOverride=$heightOverride, items=${items.size}, base=${super.toString()})"
  }

  override
  val heightRatio by lazy {
    val ratio = when (direction) {
      Row -> items.maxOfOrNull { it.heightRatio }
        ?: 0.0
      Col -> items.sumOf { it.heightRatio }
    }

    if (ratio == 0.0)
      ratio
    else heightOverride ?: ratio
  }

  override
  val widthRatio by lazy {
    val ratio = when (direction) {
      Row -> items.sumOf { it.widthRatio }
      Col -> items.maxOfOrNull { it.widthRatio }
        ?: 0.0
    }

    if (ratio == 0.0)
      ratio
    else widthOverride ?: ratio
  }


  companion object {
    fun row(name: String? = null, block: PanelBuilder.() -> Unit) =
      PanelBuilder(name, Row, block = block).build()

    fun col(name: String? = null, block: PanelBuilder.() -> Unit) =
      PanelBuilder(name, Col, block = block).build()
  }
}

/**
 * A control panel that binds a single [Control]
 *
 * @property control the control to bind on draw
 *
 * @param style
 * @param widthRatio
 * @param heightRatio
 */
class ControlItem(
  style: ControlStyle = EmptyStyle,
  widthRatio: Double = 1.0,
  heightRatio: Double = 1.0,
  val control: Control<*>,
) : ControlPanel(control.name, style, widthRatio, heightRatio) {
  override fun toControlPanel(): ControlPanel = this
  override fun toString(): String {
    return "ControlItem(control=${control.name}, base=${super.toString()})"
  }

  constructor(
    item: ControlItem,
    style: ControlStyle? = null,
    widthRatio: Number? = null,
    heightRatio: Number? = null
  ) :
    this(
      style ?: item.style,
      widthRatio?.toDouble() ?: item.widthRatio,
      heightRatio?.toDouble() ?: item.heightRatio,
      item.control,
    )

  fun draw(sketch: BaseSketch, cp5: ControlP5, tab: ControlTab, bound: BoundRect) {
    control.applyToControl(
      sketch = sketch,
      controlP5 = cp5,
      tab = cp5.getTab(tab.name),
      panel = this,
      bound = bound,
    )
  }
}
