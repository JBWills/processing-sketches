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
import util.pointsAndLines.PolyLine
import util.pointsAndLines.addPoints
import util.pointsAndLines.forEachSegment

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

val Segment.getMidpointPointClosestToThreshold: (noise: Noise, threshold: Double) -> Point
  get() = { noise: Noise, threshold: Double ->
    if (length < MIN_SEGMENT_LENGTH) midPoint
    else {
      fun Point.inside() = belowThreshold(noise, threshold)

      val s = if (p1.inside()) this else flip()

      if (s.midPoint.inside())
        Segment(midPoint, s.p2).getMidpointPointClosestToThreshold(noise, threshold)
      else
        Segment(s.p1, midPoint).getMidpointPointClosestToThreshold(noise, threshold)
    }
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
        val midPoint = segment.getMidpointPointClosestToThreshold(noise, threshold)
        curr.addAll(listOf(midPoint, segment.p2))
      }
      Exiting -> {
        val midPoint = segment.getMidpointPointClosestToThreshold(noise, threshold)

        curr.addPoints(segment.p1, midPoint)

        completeCurrSegment()
      }
      InOutIn -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = first.getMidpointPointClosestToThreshold(noise, threshold)

        curr.addPoints(first.p1, firstMidPoint)

        completeCurrSegment()

        val secondMidPoint = second.getMidpointPointClosestToThreshold(noise, threshold)
        curr.addAll(listOf(secondMidPoint, second.p2))
      }
      OutInOut -> {
        val (first, second) = segment.splitAtMidpoint()
        val firstMidPoint = first.getMidpointPointClosestToThreshold(noise, threshold)
        val secondMidPoint = second.getMidpointPointClosestToThreshold(noise, threshold)
        curr.addPoints(firstMidPoint, secondMidPoint)
        completeCurrSegment()
      }
    }
  }

  completeCurrSegment()

  return result
}
