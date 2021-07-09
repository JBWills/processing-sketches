package controls.panels.panelext

import controls.Control.FileName
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.fileSelect(
  ref: KMutableProperty0<String?>,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) {
    FileName(ref.name, ref.get()) {
      ref.set(it)
      markDirtyIf(shouldMarkDirty)
    }
  }
}
