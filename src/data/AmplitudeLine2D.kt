package data

import coordinate.Point
import util.atAmountAlong
import util.interpolation.CubicSpline2D
import util.interpolation.DefaultAmplitudeSampleDist
import util.interpolation.Interpolator2D
import util.numbers.ifNan
import util.polylines.PolyLine
import util.polylines.iterators.walkWithCursor


data class AmplitudeLine2D(
  val origValues: Array<DoubleArray>,
  val interpolator: Interpolator2D = CubicSpline2D(),
  val lengthBetweenValues: Point = Point(DefaultAmplitudeSampleDist, DefaultAmplitudeSampleDist),
  val scaleFactor: Double = 0.0,
) {
  val origValuesWidth = origValues.size
  val origValuesHeight = origValues.firstOrNull()?.size ?: 0

  val pitchAxisLength = origValues.firstOrNull()?.size ?: 0
  val timeAxisLength = origValues.size

  init {
    interpolator.setData(origValues, lengthBetweenValues.x, lengthBetweenValues.y)
  }

  private val defaultEndpoint
    get() = lengthBetweenValues * (Point(origValuesWidth, origValuesHeight) - 1)

  private fun getAtPercent(percentThroughSong: Double, hzPercent: Double) =
    interpolator
      .interpolate(
        (0.0..timeAxisLength.toDouble()).atAmountAlong(percentThroughSong),
        (0.0..pitchAxisLength.toDouble()).atAmountAlong(hzPercent),
      )
      .ifNan { 0.0 }
      .times(scaleFactor)

  fun interpolateAlong(
    poly: PolyLine,
    percentThroughSong: Double,
    step: Double,
    block: (pointOnPoly: Point, transformedPoint: Point) -> Point = { _, transformedPoint -> transformedPoint }
  ): PolyLine = poly.walkWithCursor(step) {
    val interpolationMove =
      it.segment.normal().slope.unitVector * getAtPercent(percentThroughSong, it.percent)
    block(it.point, it.point + interpolationMove)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AmplitudeLine2D

    if (!origValues.contentEquals(other.origValues)) return false

    return true
  }

  override fun hashCode(): Int {
    return origValues.contentHashCode()
  }
}
