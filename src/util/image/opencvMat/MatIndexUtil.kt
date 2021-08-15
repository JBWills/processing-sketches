package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.bytedeco.opencv.global.opencv_core
import org.opencv.core.Mat
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvMat.BorderType.BorderReflect101

fun Mat.contains(col: Int, row: Int) = contains(Point(col, row))
fun Mat.contains(p: Point) = bounds.contains(p)

fun Mat.get(p: Point, band: Int = 0): Double? = (p.x.toInt() to p.y.toInt()).let { (col, row) ->
  if (!contains(col, row)) return null
  get(row, col)?.get(band)?.let { if (it.isNaN()) null else it }
}

fun Mat.getOr(p: Point, default: Double, band: Int = 0): Double = get(p, band) ?: default
fun Mat.getInt(p: Point): Int = getFormat().toIntValue(getByteArray(p))

fun Mat.getSubPix(p: Point, band: Int = 0): Double? {
  val x = p.xi
  val y = p.yi
  if (!contains(p)) return null

  fun interp(v: Int, xAxis: Boolean) =
    opencv_core.borderInterpolate(v, if (xAxis) cols() else rows(), BorderReflect101.type)

  val topLeft = Point(interp(x, true), interp(y, false))
  val topRight = Point(interp(x + 1, true), interp(y + 1, false))

  val bounds = BoundRect(topLeft, topRight)

  fun getValue(p: Point) = get(p, band)!!

  return interpolationSubPixel(
    p,
    getValue(bounds.topLeft),
    getValue(bounds.topRight),
    getValue(bounds.bottomLeft),
    getValue(bounds.bottomRight),
  )
}

private fun interpolationSubPixel(
  interpolationXy: Point,
  topLeft: Double,
  topRight: Double,
  bottomLeft: Double,
  bottomRight: Double
): Double {
  val (xDecimal, yDecimal) = interpolationXy.map { it - it.toInt() }

  val topWeighted = (topLeft * (1.0 - xDecimal) + topRight * xDecimal) * (1.0 - yDecimal)
  val bottomWeighted = (bottomLeft * (1.0 - xDecimal) + bottomRight * xDecimal) * yDecimal

  return topWeighted + bottomWeighted
}
