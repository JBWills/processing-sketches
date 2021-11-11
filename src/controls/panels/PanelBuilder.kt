package controls.panels

import controls.panels.ListDirection.Col
import controls.panels.ListDirection.Row
import controls.props.GenericProp
import controls.props.PropData
import kotlin.reflect.KMutableProperty0

open class PanelBuilder(val panels: MutableList<Panelable>) {
  var name: String? = null
  var direction: ListDirection = Col
  var style: ControlStyle? = null
  var heightRatio: Number? = null
  var widthRatio: Number? = null

  constructor(
    name: String? = null,
    direction: ListDirection? = null,
    style: ControlStyle? = null,
    heightRatio: Number? = null,
    widthRatio: Number? = null,
  ) : this(mutableListOf()) {
    this.name = name ?: this.name
    this.direction = direction ?: this.direction
    this.style = style ?: this.style
    this.heightRatio = heightRatio ?: this.heightRatio
    this.widthRatio = widthRatio ?: this.widthRatio
  }

  constructor(
    name: String? = null,
    direction: ListDirection? = null,
    style: ControlStyle? = null,
    heightRatio: Number? = null,
    widthRatio: Number? = null,
    block: PanelBuilder.() -> Unit,
  ) : this(
    name,
    direction,
    style,
    heightRatio,
    widthRatio,
  ) {
    apply(block)
  }

  operator fun Panelable.unaryPlus() = panels.add(this)
  operator fun List<Panelable>.unaryPlus() = panels.addAll(this)
  operator fun Array<Panelable>.unaryPlus() = panels.addAll(this)

  private fun Panelable.applyAndAdd(style: ControlStyle? = null) =
    applyStyleOverrides(style).also { panels.add(it) }

  fun addNewPanel(
    style: ControlStyle? = null,
    panelCreator: PanelBuilder.() -> Panelable
  ): Panelable =
    panelCreator().applyAndAdd(style)

  fun build(): ControlList = ControlList(
    name = name ?: direction.name,
    style = style ?: ControlStyle.EmptyStyle,
    direction = direction,
    widthOverride = widthRatio?.toDouble(),
    heightOverride = heightRatio?.toDouble(),
    items = panels.toTypedArray(),
  )

  fun createAndAdd(
    name: String? = null,
    style: ControlStyle? = null,
    direction: ListDirection,
    block: PanelBuilder.() -> Unit
  ): Panelable =
    PanelBuilder(name, direction, style = style, block = block).build().applyAndAdd()

  fun row(name: String? = null, style: ControlStyle? = null, block: PanelBuilder.() -> Unit) =
    createAndAdd(name, style, Row, block)

  fun col(name: String? = null, style: ControlStyle? = null, block: PanelBuilder.() -> Unit) =
    createAndAdd(name, style, Col, block)

  fun <T : PropData<T>> panel(
    ref: KMutableProperty0<T>,
    style: ControlStyle? = null,
  ) = addNewPanel(style) {
    GenericProp(ref) {
      ref.get().asControlPanel()
    }
  }

  companion object {
    internal fun list(block: PanelBuilder.() -> Unit): ControlList =
      PanelBuilder().apply(block).build()
  }
}
