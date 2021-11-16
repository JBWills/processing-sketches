package util.polylines

import Interpolator2D.CubicSpline2D
import coordinate.Point

/**
 * TODO Figure out why this is creating really wild values
 *
 * @param step
 * @return
 */
fun PolyLine.interpolate(step: Double = 1.0): PolyLine {
  val interpolator = CubicSpline2D
  interpolator.setData(
    DoubleArray(this.size) { i -> this[i].x },
    DoubleArray(this.size) { i -> this[i].y },
  )

  return walkWithPercentAndSegment(step) { percent, segment, point ->
    val interpolatedValue = interpolator.interpolate(point.x)
    if (interpolatedValue.isNaN()) point else Point(point.x, interpolatedValue)
  }
}
