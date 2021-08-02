package util.image.opencvMat

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar

typealias ScalarFn = (Mat, Scalar, Mat) -> Unit

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
