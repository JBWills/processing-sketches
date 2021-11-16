package util.interpolation

import util.base.DoubleRange
import util.iterators.minMax

val DefaultAmplitudeSampleDist = 1.0

fun getSpacingForDataPoints(data: DoubleArray): Double {
  val ampMinMax: DoubleRange = data.minMax ?: 0.0..1.0

  return ampMinMax.endInclusive - ampMinMax.start
}

fun amplitudeToPoints(
  yValues: DoubleArray,
  dist: Double = DefaultAmplitudeSampleDist
): Pair<DoubleArray, DoubleArray> = DoubleArray(yValues.size) { i -> i * dist } to yValues
