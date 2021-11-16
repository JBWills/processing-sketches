package util.polylines

import Interpolator2D.CubicSpline2D
import coordinate.Point
import util.atAmountAlong

fun PolyLine.interpolate(step: Double = 1.0): PolyLine {
  val interpolator = CubicSpline2D
  interpolator.setData(
    DoubleArray(this.size) { i -> this[i].x },
    DoubleArray(this.size) { i -> this[i].y },
  )

  val xRange = first().x..last().x

  return walkWithPercentAndSegment(step) { percent, segment, point ->
    val x = xRange.atAmountAlong(percent)
    val interpolatedValue = interpolator.interpolate(x)
    if (interpolatedValue.isNaN()) point else Point(x, interpolatedValue)
  }
}
