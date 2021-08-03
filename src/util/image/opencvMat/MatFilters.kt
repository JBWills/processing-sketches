package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import util.coerceOdd
import util.image.ImageFormat
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.ImageFormat.RGB
import util.image.converted
import util.image.opencvMat.OpenCVThresholdType.ThreshBinary
import java.awt.Color

/**
 * Helper to apply a function to a mat with a newly created destination mat.
 *
 * @param format the image format of the new mat.
 * @param block the operation to perform
 * @return the new mat.
 */
fun Mat.applyWithDest(
  format: ImageFormat = getFormat(),
  inPlace: Boolean = false,
  block: (src: Mat, dest: Mat) -> Unit,
): Mat = (if (inPlace) this else cloneEmpty(format)).also { dest -> block(this, dest) }

/**
 * Canny filter a binary image (U8 gray image where 255 is white)
 */
fun Mat.cannyBinary(): Mat = canny(245.0)

fun Mat.canny(threshold: Double): Mat = applyWithDest(Gray) { src, dest ->
  Imgproc.Canny(src, dest, threshold, threshold * CannyRatio, 3, true)
}

fun Mat.threshold(value: Double, type: OpenCVThresholdType = ThreshBinary): Mat =
  applyWithDest(Gray) { src, dest ->
    Imgproc.threshold(src, dest, value, 1.0, type.typeVal)
    dest.convertTo(dest, ChannelDepth.CV_8U.typeVal, 255.0)
  }

fun Mat.submat(bounds: BoundRect): Mat =
  submat(Rect(bounds.leftPx, bounds.topPx, bounds.widthPx + 1, bounds.heightPx + 1))

fun Mat.gaussianBlur(
  radius: Point,
  sigma: Double = radius.magnitude / 2.0,
  inPlace: Boolean = false
): Mat = applyWithDest(inPlace = inPlace) { src, dest ->
  val size = radius.map(Double::coerceOdd).toSize()
  Imgproc.GaussianBlur(src, dest, size, sigma)
}

fun Mat.gaussianBlur(radius: Int, sigma: Double = radius / 2.0, inPlace: Boolean = false): Mat =
  gaussianBlur(Point(radius, radius), sigma, inPlace)

/**
 * Blur only the alpha channel of the image. and assign all color values to a solid color.
 * @return a new mat.
 */
fun Mat.gaussianBlurAlpha(radius: Int, fillColor: Color, sigma: Double = radius / 2.0): Mat {
  if (getFormat() != ARGB) {
    throw Exception("Tried to call gaussianBlurAlpha with non-ARGB format: ${getFormat()}")
  }

  val alphaChannel = split()[0]
  val rgbFill = createMat(rows(), cols(), RGB, fillColor)

  setChannels(alphaChannel.gaussianBlur(radius, sigma), *rgbFill.splitArray())

  return this
}

/**
 * Given an alpha mat, convert it to an ARGB mat with the given fill color.
 */
fun Mat.asDisplayAlpha(fillColor: Color): Mat {
  if (channels() != 1) {
    throw Exception("Trying to use non-single channel Mat as alpha mat. Expected channel: 1. Actual channels: ${channels()}")
  }

  val maxAlpha = fillColor.alpha / 255.0
  val newAlpha = multiply(maxAlpha, inPlace = false)

  val fillMat = createMat(rows(), cols(), RGB, fillColor)
  return (listOf(newAlpha) + fillMat.split()).merge(ARGB)
}

/**
 * convert an ARGB image to a grayscale image
 *
 * @param fillColor
 * @return
 */
fun Mat.luminanceToAlpha(fillColor: Color): Mat =
  converted(ARGB, Gray).asDisplayAlpha(fillColor)