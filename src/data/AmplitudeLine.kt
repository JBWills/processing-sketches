package data

import coordinate.Point
import util.atAmountAlong
import util.interpolation.Interpolator1D
import util.interpolation.Interpolator1D.CubicSpline1D
import util.interpolation.getSpacingForDataPoints
import util.numbers.ifNan
import util.polylines.PolyLine
import util.polylines.walkWithPercentAndSegment


data class AmplitudeLine(
  val origValues: DoubleArray,
  val interpolator: Interpolator1D = CubicSpline1D,
  val lengthBetweenValues: Double = getSpacingForDataPoints(origValues),
  val scaleFactor: Double = 0.0,
) {
  init {
    interpolator.setData(origValues, lengthBetweenValues)
  }

  private val defaultXEndpoint get() = lengthBetweenValues * (origValues.size - 1)

  val meanAmplitude by lazy { origValues.average() * scaleFactor }

  fun getUninterpolatedLine(xLength: Double) =
    origValues.mapIndexed { index, yVal ->
      Point((index / (origValues.size.toDouble() - 1)) * xLength, yVal)
    }

  private fun getAtPercent(percent: Double) =
    interpolator
      .interpolate((0.0..defaultXEndpoint).atAmountAlong(percent))
      .ifNan { 1.0 }
      .times(scaleFactor)

  fun interpolateAlong(
    poly: PolyLine,
    step: Double,
    block: (pointOnPoly: Point, transformedPoint: Point) -> Point = { _, transformedPoint -> transformedPoint }
  ): PolyLine = poly.walkWithPercentAndSegment(step) { percent, segment, point ->
    val interpolationMove = segment.normal().slope.unitVector * getAtPercent(percent)
    block(point, point + interpolationMove)
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
