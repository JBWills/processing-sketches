package util.image.opencvContouring

import coordinate.BoundRect
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.image.ChannelDepth
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.OpenCVThresholdType
import util.image.OpenCVThresholdType.ThreshBinary
import util.image.cloneEmpty

typealias ScalarFn = (Mat, Scalar, Mat) -> Unit

fun Mat.convertTo(
  depth: ChannelDepth,
  dest: Mat = this,
  alpha: Double? = null,
  beta: Double? = null
): Mat {
  when {
    alpha != null && beta != null -> convertTo(dest, depth.typeVal, alpha, beta)
    alpha != null -> convertTo(dest, depth.typeVal, alpha)
    else -> convertTo(dest, depth.typeVal)
  }

  return dest
}

fun Mat.createScalar(value: Double) = when (channels()) {
  1 -> Scalar(value)
  2 -> Scalar(value, value)
  3 -> Scalar(value, value, value)
  else -> Scalar(value, value, value, value)
}

private fun Mat.perform(fn: ScalarFn, s: Scalar, inPlace: Boolean = false): Mat {
  val dest = if (inPlace) this else cloneEmpty()
  fn(this, s, dest)
  return dest
}

fun Mat.multiply(i: Double, inPlace: Boolean = false): Mat =
  perform(Core::multiply, createScalar(i), inPlace)

fun Mat.divide(i: Double, inPlace: Boolean = false): Mat =
  perform(Core::divide, createScalar(i), inPlace)

fun Mat.add(i: Double, inPlace: Boolean = false): Mat =
  perform(Core::add, createScalar(i), inPlace)

fun Mat.subtract(i: Double, inPlace: Boolean = false): Mat =
  perform(Core::subtract, createScalar(i), inPlace)

fun Mat.applyWithDest(
  format: ImageFormat = getFormat(),
  block: (src: Mat, dest: Mat) -> Unit,
): Mat = cloneEmpty(format).also { dest -> block(this, dest) }

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
