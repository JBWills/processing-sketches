package controls.panels.panelext

import controls.Control.Toggle
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.toggle(
  ref: KMutableProperty0<Boolean>,
  style: ControlStyle? = null
) = addNewPanel(style) {
  GenericProp(ref) { Toggle(ref, text = ref.name) { markDirty() } }
}
