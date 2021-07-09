package util.interpolation

import util.DoubleRange
import util.atAmountAlong

typealias InterpolationFn = (Double) -> Double

val EaseIn: InterpolationFn = { t ->
  t * t
}

fun DoubleRange.interpolate(
  linearPercent: Double,
  easingFunction: (Double) -> Double = { it }
): Double = atAmountAlong(easingFunction(linearPercent))
