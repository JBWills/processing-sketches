package util.image.opencvMat.filters

import org.opencv.core.Mat
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvMat.applyWithDest

fun Mat.clamp(min: Number = 0.0, max: Number = 255.0, inPlace: Boolean = false): Mat =
  applyWithDest(inPlace = inPlace) { src, dest ->
    dest
      .subtract(src.getFormat().doubleToScalar(min.toDouble(), alpha = 0.0), inPlace = true)
      .multiply(
        src.getFormat().doubleToScalar(255 / (max.toDouble() - min.toDouble()), alpha = 1.0),
        inPlace = true,
      )
  }
