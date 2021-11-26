package util.interpolation

val DefaultAmplitudeSampleDist = 1.0

fun amplitudeToPoints(
  yValues: DoubleArray,
  dist: Double = DefaultAmplitudeSampleDist
): Pair<DoubleArray, DoubleArray> = DoubleArray(yValues.size) { i -> i * dist } to yValues
