package util.algorithms.contouring

import arrow.core.memoize
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import org.opencv.core.Mat
import util.algorithms.contouring.SegmentContourStatus.Companion.getStatus
import util.algorithms.contouring.SegmentContourStatus.Entering
import util.algorithms.contouring.SegmentContourStatus.Exiting
import util.algorithms.contouring.SegmentContourStatus.InOutIn
import util.algorithms.contouring.SegmentContourStatus.Inside
import util.algorithms.contouring.SegmentContourStatus.OutInOut
import util.algorithms.contouring.SegmentContourStatus.Outside
import util.image.opencvMat.get
import util.polylines.addPoints
import util.polylines.forEachSegment
import util.polylines.polyLine.PolyLine

const val MIN_SEGMENT_LENGTH = 1

private fun Point.belowThreshold(noise: Noise, threshold: Double) =
  noise.getPositive(x, y) < threshold


private enum class SegmentContourStatus {
  Inside,
  Outside,
  Entering,
  Exiting,
  InOutIn,
  OutInOut,
  ;

  companion object {
    fun Segment.getStatus(isUnderThreshold: (Point) -> Boolean): SegmentContourStatus {
      val p1Inside = isUnderThreshold(p1)
      val midInside = isUnderThreshold(midPoint)
      val p2Inside = isUnderThreshold(p2)
      return when {
        p1Inside && !midInside && p2Inside -> InOutIn
        !p1Inside && midInside && !p2Inside -> OutInOut
        p1Inside && p2Inside -> Inside
        !p1Inside && p2Inside -> Entering
        p1Inside && !p2Inside -> Exiting
        else -> Outside
      }
    }
  }
}

fun Segment.getPointAtThreshold(isUnderThreshold: (Point) -> Boolean): Point {
  if (length < MIN_SEGMENT_LENGTH) return midPoint

  fun Point.inside() = isUnderThreshold(this)

  val s = if (p1.inside()) this else flip()

  return if (s.midPoint.inside()) {
    Segment(s.midPoint, s.p2)
  } else {
    Segment(s.p1, s.midPoint)
  }.getPointAtThreshold(isUnderThreshold)
}

val _getPointAtThresholdMemoized = { s: Segment, isUnderThreshold: (Point) -> Boolean ->
  s.getPointAtThreshold(isUnderThreshold)
}.memoize()

fun PolyLine.walkThreshold(isUnderThreshold: (Point) -> Boolean): List<PolyLine> {
  val result = mutableListOf<PolyLine>()

  var curr = mutableListOf<Point>()

  fun completeCurrSegment() {
    if (curr.isNotEmpty()) {
      result.add(curr)
      curr = mutableListOf()
    }
  }

  forEachSegment { segment ->
    when (segment.getStatus(isUnderThreshold)) {
      Inside -> curr.addPoints(segment.p1, segment.p2)
      Outside -> completeCurrSegment()
      Entering -> {
        completeCurrSegment()
        val midPoint = _getPointAtThresholdMemoized(segment, isUnderThreshold)
        curr.addAll(listOf(midPoint, segment.p2))
      }
      Exiting -> {
        val midPoint = _getPointAtThresholdMemoized(segment, isUnderThreshold)

        curr.addPoints(segment.p1, midPoint)

        completeCurrSegment()
      }
      InOutIn -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = _getPointAtThresholdMemoized(first, isUnderThreshold)

        curr.addPoints(first.p1, firstMidPoint)

        completeCurrSegment()

        val secondMidPoint = _getPointAtThresholdMemoized(second, isUnderThreshold)
        curr.addAll(listOf(secondMidPoint, second.p2))
      }
      OutInOut -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = _getPointAtThresholdMemoized(first, isUnderThreshold)
        val secondMidPoint = _getPointAtThresholdMemoized(second, isUnderThreshold)
        curr.addPoints(firstMidPoint, secondMidPoint)
        completeCurrSegment()
      }
    }
  }

  completeCurrSegment()

  return result
}

fun PolyLine.walkThreshold(noise: Noise, threshold: Double): List<PolyLine> = walkThreshold {
  it.belowThreshold(noise, threshold)
}

fun PolyLine.walkThreshold(mat: Mat, threshold: Double, band: Int = 0): List<PolyLine> =
  walkThreshold {
    val value = mat.get(it, band) ?: (threshold + 1)
    value < threshold
  }
