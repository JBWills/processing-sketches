package controls.props.types

import controls.Control.ImageFile
import controls.props.GenericProp
import util.image.ImageCrop
import util.image.ImageCrop.Fill
import kotlin.reflect.KMutableProperty0

fun imageFileProp(
  ref: KMutableProperty0<String>,
  thumbnailCrop: ImageCrop = Fill,
) = GenericProp(ref) {
  ImageFile(ref.name, ref.get(), thumbnailCrop) {
    ref.set(it ?: "")
    markDirty()
  }
}
