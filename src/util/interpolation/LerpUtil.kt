package util.interpolation

import util.atAmountAlong
import util.base.DoubleRange

typealias InterpolationFn = (Double) -> Double

val EaseIn: InterpolationFn = { t ->
  t * t
}

fun DoubleRange.interpolate(
  linearPercent: Double,
  easingFunction: (Double) -> Double = { it }
): Double = atAmountAlong(easingFunction(linearPercent))
