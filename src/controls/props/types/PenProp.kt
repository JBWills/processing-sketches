package controls.props.types

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.panels.ControlTab
import controls.panels.PanelBuilder
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import kotlinx.serialization.Serializable
import util.print.Gelly1
import util.print.Pen
import util.print.PenType
import util.print.StrokeWeight
import util.print.Style

@Serializable
data class PenProp(
  var penType: PenType = PenType.GellyColor,
  var pen: Pen = Pen.GellyColorWhite,
  var weight: StrokeWeight = Gelly1(),
) : PropData<PenProp> {

  val style: Style get() = pen.style.applyOverrides(Style(weight = weight))

  constructor(penProp: PenProp) : this(
    penProp.penType,
    penProp.pen,
    penProp.weight,
  )

  override fun toSerializer() = serializer()

  override fun clone() = PenProp(this)

  fun penPanel(
    builder: PanelBuilder,
    updateControlsOnPenChange: Boolean = false,
    filterByWeight: Boolean = false
  ) = builder.apply {
    row {
      if (filterByWeight) {
        dropdown(::weight, options = Pen.AllWeights, getName = { it.name }) { updateControls() }
        dropdown(
          ::pen,
          options = Pen.withThickness(weight),
          getName = { it.colorName },
        ) {
          penType = it.data.type
          if (updateControlsOnPenChange) updateControls()
        }
      } else {
        dropdown(::penType) {
          if (pen.type != penType) {
            pen = Pen.withType(penType)[0]
          }
          updateControls()
        }
        dropdown(::pen, options = Pen.withType(penType), getName = { it.colorName }) {
          if (updateControlsOnPenChange) updateControls()
        }
        dropdown(::weight, options = pen.weights.toList(), getName = { it.name })
      }
    }
  }

  override fun bind(): List<ControlTab> = singleTab("Photo") { penPanel(this) }
}
