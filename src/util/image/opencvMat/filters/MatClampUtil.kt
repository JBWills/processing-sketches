package util.image.opencvMat.filters

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import util.image.opencvMat.copy

fun Mat.clamp(min: Number = 0.0, max: Number = 255.0, inPlace: Boolean = false): Mat {
  val destMat = if (inPlace) this else copy()

  Core.subtract(destMat, Scalar(min.toDouble()), destMat)
  Core.multiply(destMat, Scalar(255 / (max.toDouble() - min.toDouble())), destMat)
  return destMat
}
