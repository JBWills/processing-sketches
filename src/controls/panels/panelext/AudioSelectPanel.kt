package controls.panels.panelext

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import data.Audio
import kotlin.reflect.KMutableProperty0


fun PanelBuilder.audioSelect(
  ref: KMutableProperty0<Audio>,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = col(style = style) {
  fileSelect(ref.get()::filename, shouldMarkDirty = shouldMarkDirty)
  row {
    slider(ref.get()::startPercent, shouldMarkDirty = shouldMarkDirty)
    slider(ref.get()::endPercent, shouldMarkDirty = shouldMarkDirty)
  }
}
