package util.pointsAndLines.polyLine

import coordinate.Point
import coordinate.Segment
import geomerativefork.src.util.boundMin
import util.DoubleRange
import util.debugLog

fun PolyLine.normalizeForPrint() = normalizeDistances(1.5..1000.0)

/**
 * Given a polyline with segments of varying distances, combine some segments and split others so
 * that all of the distances are in the given range.
 *
 * @param distRange
 * @return
 */
fun PolyLine.normalizeDistances(distRange: DoubleRange, angleCutoff: Double = 45.0): PolyLine {
  val distRangeStartClipped = distRange.start.boundMin(0.0)
  val distRangeClipped =
    distRangeStartClipped..distRange.endInclusive.boundMin(distRangeStartClipped + 0.1)
  val result = mutableListOf<Segment>()
  var lastSegment: Segment? = null
  var lastPoint: Point? = null
  forEachSegment { segment ->
//    val lastSegment = lastPoint?.lineTo(segment.p1)
    lastSegment?.let { lastNonNull ->
      if (lastNonNull.angleBetween(segment) > angleCutoff) {
        result.add(lastNonNull)
        lastSegment = null
      }
    }

    val newSegment = lastSegment?.combine(segment) ?: segment

    lastSegment = when {
      newSegment.length in distRangeClipped -> {
        result.add(newSegment)
        null
      }
      newSegment.length < distRangeClipped.start -> {
        debugLog("here2 \nlas: $lastSegment\nseg: $segment \nnew: $newSegment")
        newSegment
      }
      else -> {
        val splitSegments: List<Segment> =
          newSegment.walk(distRangeClipped.endInclusive).toSegments()
        result.addAll(splitSegments.dropLast(1))

        val last = splitSegments.last()

        debugLog("here3")
        if (last.length in distRangeClipped) {
          result.add(last)
          debugLog("here4")
          null
        } else {
          debugLog("here5")
          last
        }
      }
    }
  }

  lastSegment?.let { result.add(it) }

  return result.toPolyLine()
}
