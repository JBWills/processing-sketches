package util.polylines

import PointInterpolator1D.CubicSpline2D
import coordinate.Point

fun PolyLine.interpolate(step: Double = 1.0): PolyLine {
  var last: Point? = null
  val filteredLine = filter {
    val result: Boolean = it.x != last?.x
    last = it
    result
  }
  if (filteredLine.size < 3) return this
  val interpolator = CubicSpline2D
  interpolator.setData(filteredLine)

  return filteredLine.walkWithPercentAndSegment(step) { _, _, point ->
    val interpolatedValue = interpolator.interpolate(point.x)
    if (interpolatedValue.isNaN()) point else Point(point.x, interpolatedValue)
  }
}
