package util.image

import coordinate.Point
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import util.coerceOdd
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.ImageFormat.RGB
import java.awt.Color

fun Mat.gaussianBlur(radius: Point, sigma: Double = radius.magnitude / 2.0): Mat {
  val destMat = asBlankMat()
  val size = radius.map(Double::coerceOdd).toSize()
  Imgproc.GaussianBlur(this, destMat, size, sigma)

  return destMat
}

fun Mat.gaussianBlur(radius: Int, sigma: Double = radius / 2.0): Mat =
  gaussianBlur(Point(radius, radius), sigma)

fun Mat.gaussianBlurAlpha(radius: Int, fillColor: Color, sigma: Double = radius / 2.0): Mat {
  if (getFormat() != ARGB) {
    throw Exception("Tried to call gaussianBlurAlpha with non-ARGB format: ${getFormat()}")
  }

  val alphaChannel = split()[0]
  val rgbFill = createMat(rows(), cols(), RGB, fillColor)

  setChannels(alphaChannel.gaussianBlur(radius, sigma), *rgbFill.splitArray())

  return this
}


fun Mat.luminanceToAlpha(fillColor: Color): Mat {
  val maxAlpha = fillColor.alpha / 255.0
  val gray = converted(ARGB, Gray) * maxAlpha
  val redMat = createMat(rows(), cols(), RGB, fillColor)

  return (listOf(gray) + redMat.split()).merge(ARGB)
}
