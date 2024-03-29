package data

import coordinate.Point
import util.atAmountAlong
import util.interpolation.CubicSpline1D
import util.interpolation.DefaultAmplitudeSampleDist
import util.interpolation.Interpolator1D
import util.numbers.ifNan
import util.polylines.PolyLine
import util.polylines.iterators.walkWithCursor


data class AmplitudeLine(
  val origValues: DoubleArray,
  val interpolator: Interpolator1D = CubicSpline1D(),
  val lengthBetweenValues: Double = DefaultAmplitudeSampleDist,
  val scaleFactor: Double = 0.0,
) {
  init {
    interpolator.setData(origValues, lengthBetweenValues)
  }

  private val defaultXEndpoint get() = lengthBetweenValues * (origValues.size - 1)

  val meanAmplitude by lazy { origValues.average() * scaleFactor }

  private fun getAtPercent(percent: Double) =
    interpolator
      .interpolate((0.0..defaultXEndpoint).atAmountAlong(percent))
      .ifNan { 1.0 }
      .times(scaleFactor)

  fun interpolateAlong(
    poly: PolyLine,
    step: Double,
    block: (pointOnPoly: Point, transformedPoint: Point) -> Point = { _, transformedPoint -> transformedPoint }
  ): PolyLine = poly.walkWithCursor(step) {
    val interpolationMove = it.segment.normal().slope.unitVector * getAtPercent(it.percent)
    block(it.point, it.point + interpolationMove)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AmplitudeLine

    if (!origValues.contentEquals(other.origValues)) return false

    return true
  }

  override fun hashCode(): Int {
    return origValues.contentHashCode()
  }
}
