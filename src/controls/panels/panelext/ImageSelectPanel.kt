package controls.panels.panelext

import controls.controlsealedclasses.ImageFile
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import util.image.ImageCrop
import util.image.ImageCrop.Fill
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.imageSelect(
  ref: KMutableProperty0<String>,
  style: ControlStyle? = null,
  thumbnailCrop: ImageCrop = Fill,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) {
    ImageFile(ref.name, ref.get(), thumbnailCrop) {
      ref.set(it ?: "")
      markDirtyIf(shouldMarkDirty)
    }
  }
}
