package util.image.opencvMat.filters

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import util.image.opencvMat.applyWithDest

fun Mat.subtract(scalar: Scalar, inPlace: Boolean = false): Mat =
  applyWithDest(inPlace = inPlace) { src, dest ->
    Core.subtract(src, scalar, dest)
  }

fun Mat.add(scalar: Scalar, inPlace: Boolean = false): Mat =
  applyWithDest(inPlace = inPlace) { src, dest ->
    Core.add(src, scalar, dest)
  }

fun Mat.multiply(scalar: Scalar, inPlace: Boolean = false): Mat =
  applyWithDest(inPlace = inPlace) { src, dest ->
    Core.multiply(src, scalar, dest)
  }

fun Mat.divide(scalar: Scalar, inPlace: Boolean = false): Mat =
  applyWithDest(inPlace = inPlace) { src, dest ->
    Core.divide(src, scalar, dest)
  }
