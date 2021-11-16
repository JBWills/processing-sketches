package data

import coordinate.Point
import util.atAmountAlong
import util.interpolation.Interpolator1D
import util.interpolation.Interpolator1D.CubicSpline1D
import util.interpolation.getSpacingForDataPoints
import util.polylines.PolyLine
import util.polylines.walkWithPercentAndSegment


data class AmplitudeLine(
  val origValues: DoubleArray,
  val interpolator: Interpolator1D = CubicSpline1D,
  val lengthBetweenValues: Double = getSpacingForDataPoints(origValues)
) {
  init {
    interpolator.setData(origValues, lengthBetweenValues)
  }

  private val defaultXEndpoint get() = lengthBetweenValues * (origValues.size - 1)

  fun getUninterpolatedLine(xLength: Double) =
    origValues.mapIndexed { index, yVal ->
      Point((index / (origValues.size.toDouble() - 1)) * xLength, yVal)
    }

  private fun getAtPercent(percent: Double) =
    interpolator.interpolate((0.0..defaultXEndpoint).atAmountAlong(percent))

  fun interpolateAlong(
    poly: PolyLine,
    step: Double,
    block: (pointOnPoly: Point, transformedPoint: Point) -> Point = { _, transformedPoint -> transformedPoint }
  ): PolyLine = poly.walkWithPercentAndSegment(step) { percent, segment, point ->
    val newPoint = point + segment.normal().slope.unitVector * getAtPercent(percent)
    block(point, newPoint)
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
