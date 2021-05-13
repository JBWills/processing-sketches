package controls.panels.panelext

import controls.Control.Slider
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import coordinate.Deg
import util.DoubleRange
import kotlin.reflect.KMutableProperty0

private fun degreeSliderProp(
  ref: KMutableProperty0<Deg>,
  range: DoubleRange = 0.0..360.0,
) = GenericProp(ref) {
  Slider(ref.name, range, ref.get().value) {
    ref.set(Deg(it))
    markDirty()
  }
}

fun PanelBuilder.degreeSlider(
  ref: KMutableProperty0<Deg>,
  range: DoubleRange = 0.0..360.0,
  style: ControlStyle? = null,
) = addNewPanel(style) { degreeSliderProp(ref, range) }
