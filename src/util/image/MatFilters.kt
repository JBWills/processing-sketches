package util.image

import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.coerceOdd
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.ImageFormat.RGB
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.toMatOfPointList
import java.awt.Color

@JvmName("fillPolyMulti")
fun Mat.fillPoly(polys: List<PolyLine>, color: Scalar = Scalar(255.0)) =
  Imgproc.fillPoly(this, polys.toMatOfPointList(), color)

@JvmName("fillPolyMultiMulti")
fun Mat.fillPoly(polys: List<List<PolyLine>>, color: Scalar = Scalar(255.0)) =
  fillPoly(polys.flatten(), color)

fun Mat.fillPoly(poly: PolyLine, color: Scalar = Scalar(255.0)) = fillPoly(listOf(poly), color)

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

fun Mat.luminanceToAlpha(fillColor: Color): Mat =
  converted(ARGB, Gray).asDisplayAlpha(fillColor)

fun Mat.asDisplayAlpha(fillColor: Color): Mat {
  if (channels() != 1) {
    throw Exception("Trying to use non-single channel Mat as alpha mat. Expected channel: 1. Actual channels: ${channels()}")
  }

  val maxAlpha = fillColor.alpha / 255.0
  val newAlpha = this * maxAlpha

  val fillMat = createMat(rows(), cols(), RGB, fillColor)
  return (listOf(newAlpha) + fillMat.split()).merge(ARGB)
}
