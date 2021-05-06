package util.algorithms.contouring

import arrow.core.memoize
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import util.algorithms.contouring.SegmentContourStatus.Companion.getStatus
import util.algorithms.contouring.SegmentContourStatus.Entering
import util.algorithms.contouring.SegmentContourStatus.Exiting
import util.algorithms.contouring.SegmentContourStatus.InOutIn
import util.algorithms.contouring.SegmentContourStatus.Inside
import util.algorithms.contouring.SegmentContourStatus.OutInOut
import util.algorithms.contouring.SegmentContourStatus.Outside
import util.pointsAndLines.mutablePolyLine.addPoints
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.forEachSegment

val MIN_SEGMENT_LENGTH = 1

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
    fun Segment.getStatus(noise: Noise, threshold: Double): SegmentContourStatus {
      val p1Inside = p1.belowThreshold(noise, threshold)
      val midInside = midPoint.belowThreshold(noise, threshold)
      val p2Inside = p2.belowThreshold(noise, threshold)
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

fun Segment.getPointAtThreshold(noise: Noise, threshold: Double): Point {
  if (length < MIN_SEGMENT_LENGTH) return midPoint

  fun Point.inside() = belowThreshold(noise, threshold)

  val s = if (p1.inside()) this else flip()

  return if (s.midPoint.inside()) {
    Segment(s.midPoint, s.p2)
  } else {
    Segment(s.p1, s.midPoint)
  }.getPointAtThreshold(noise, threshold)
}

val _getPointAtThresholdMemoized = { s: Segment, noise: Noise, threshold: Double ->
  s.getPointAtThreshold(noise, threshold)
}.memoize()

fun PolyLine.walkThreshold(noise: Noise, threshold: Double): List<PolyLine> {
  val result = mutableListOf<PolyLine>()

  var curr = mutableListOf<Point>()

  fun completeCurrSegment() {
    if (curr.isNotEmpty()) {
      result.add(curr)
      curr = mutableListOf()
    }
  }

  forEachSegment { segment ->
    when (segment.getStatus(noise, threshold)) {
      Inside -> curr.addPoints(segment.p1, segment.p2)
      Outside -> completeCurrSegment()
      Entering -> {
        completeCurrSegment()
        val midPoint = _getPointAtThresholdMemoized(segment, noise, threshold)
        curr.addAll(listOf(midPoint, segment.p2))
      }
      Exiting -> {
        val midPoint = _getPointAtThresholdMemoized(segment, noise, threshold)

        curr.addPoints(segment.p1, midPoint)

        completeCurrSegment()
      }
      InOutIn -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = _getPointAtThresholdMemoized(first, noise, threshold)

        curr.addPoints(first.p1, firstMidPoint)

        completeCurrSegment()

        val secondMidPoint = _getPointAtThresholdMemoized(second, noise, threshold)
        curr.addAll(listOf(secondMidPoint, second.p2))
      }
      OutInOut -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = _getPointAtThresholdMemoized(first, noise, threshold)
        val secondMidPoint = _getPointAtThresholdMemoized(second, noise, threshold)
        curr.addPoints(firstMidPoint, secondMidPoint)
        completeCurrSegment()
      }
    }
  }

  completeCurrSegment()

  return result
}
