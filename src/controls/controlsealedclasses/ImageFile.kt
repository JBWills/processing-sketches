package controls.controlsealedclasses

import BaseSketch
import controlP5.Button
import controlP5.ControlP5
import controls.utils.selectFile
import util.bounds
import util.image.ImageCrop
import util.image.ImageCrop.Fill
import util.image.pimage.pasteOnTopCentered
import util.image.pimage.scaleAndCrop
import util.image.pimage.solidColorPImage
import util.io.loadImageMemo
import util.io.noImageSelectedFilepath
import java.awt.Color
import java.io.File

class ImageFile(
  fieldName: String,
  defaultPath: String = "",
  thumbnailCrop: ImageCrop = Fill,
  onChange: BaseSketch.(String?) -> Unit,
) : Control<Button>(
  fieldName,
  ControlP5::addButton,
  { sketch, _ ->
    fun updateThumbnailAndLabel(path: String) {
      val noImageSelected =
        path.isBlank() || !File(path).exists() || path == noImageSelectedFilepath

      sketch.loadImageMemo(if (noImageSelected) noImageSelectedFilepath else path)
        ?.scaleAndCrop(thumbnailCrop, bounds)
        ?.let {
          setImage(
            it.pasteOnTopCentered(
              solidColorPImage(
                bounds.size,
                Color.PINK,
              ),
            ),
          )
        }
      label = fieldName
      setCaptionLabel(if (noImageSelected) "No Image Selected" else path)
      isLabelVisible = true
    }

    setColorBackground(Color.PINK.rgb)

    updateThumbnailAndLabel(defaultPath)
    onClick {
      sketch.selectFile { file ->
        val path = if (file == null || file.path == noImageSelectedFilepath) "" else file.path
        updateThumbnailAndLabel(path)
        sketch.onChange(path)
      }
    }
  },
)
