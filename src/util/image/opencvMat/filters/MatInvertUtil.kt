package util.image.opencvMat.filters

import org.opencv.core.Core
import org.opencv.core.Mat
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.opencvMat.copy
import util.image.opencvMat.createMat
import util.image.opencvMat.merge
import util.image.opencvMat.split
import java.awt.Color

fun Mat.invert(
  inPlace: Boolean = false,
  pureWhite: Mat = createMat(rows(), cols(), Gray, Color.WHITE)
): Mat {
  val dest = if (inPlace) this else copy()
  return dest.split().map {
    Core.subtract(pureWhite, it, it)
    it
  }.merge(getFormat())
}
