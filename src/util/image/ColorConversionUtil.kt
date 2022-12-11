package util.image

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.ArgbProcessing
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvMat.asBlankMat
import util.image.opencvMat.flags.CConv
import util.image.opencvMat.flags.CConv.RgbaToArgb
import util.image.opencvMat.merge
import util.image.opencvMat.split4

fun Mat.converted(from: ImageFormat = getFormat(), to: ImageFormat = getFormat()): Mat {
  val conv = CConv.getCConv(from, to)

  if (conv.isIdentity) return this

  if (conv == RgbaToArgb) {
    val (red, green, blue, alpha) = split4() ?: return this

    return listOf(alpha, red, green, blue).merge(ArgbProcessing)
  }

  if (conv.value == null) {
    throw Exception("Attempting to use invalid conversion type: $conv")
  }

  val newMat = asBlankMat(to.openCVFormat)
  Imgproc.cvtColor(this, newMat, conv.value)

  return newMat
}
