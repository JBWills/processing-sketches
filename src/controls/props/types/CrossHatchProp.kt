package controls.props.types

import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.slider
import controls.props.PropData
import coordinate.Deg
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CrossHatchProp(
  var lineDensity: Double = 0.1,
  var lineAngle: Deg = Deg(0),
  var lineOffset: Double = 0.0,
  @Transient val showAngleSlider: Boolean = true,
) : PropData<CrossHatchProp> {

  fun withAngle(d: Deg) = CrossHatchProp(lineDensity, d, lineOffset)

  override fun toSerializer() = serializer()

  override fun clone() = CrossHatchProp(lineDensity, lineAngle, lineOffset)

  override fun bind(): List<ControlTab> = singleTab(this::class.simpleName!!) {
    row {
      slider(::lineDensity, 0..1)
      slider(::lineOffset)
      if (showAngleSlider) slider(::lineAngle, 0.0..180.0)
    }
  }
}
