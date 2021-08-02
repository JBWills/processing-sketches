package util.image

import coordinate.BoundRect
import processing.core.PImage
import util.image.pimage.cropCentered
import util.image.pimage.scale
import util.image.pimage.scaleByHeight
import util.image.pimage.scaleByWidth
import util.image.pimage.size

enum class ImageCrop {
  Fill,
  Fit,
  Stretch,
  Crop,
  ;

  /**
   * @param im the image to crop
   * @param container the container to crop the image to
   * @return a new PImage (doesn't modify im object)
   */
  fun cropped(im: PImage, container: BoundRect): PImage {
    val sizeRatio = im.size / container.size
    val tallerAspectRatioThanContainer = sizeRatio.y >= sizeRatio.x

    return when (this) {
      Fill -> if (tallerAspectRatioThanContainer) {
        // crop height
        im.scaleByWidth(container.width.toInt())
          .cropCentered(container.size)
      } else {
        // crop width
        im.scaleByHeight(container.height.toInt())
          .cropCentered(container.size)
      }
      Fit ->
        if (tallerAspectRatioThanContainer) im.scaleByHeight(container.height.toInt())
        else im.scaleByWidth(container.width.toInt())
      Stretch -> im.scale(container.size)
      Crop -> im.cropCentered(container.size)
    }
  }
}



