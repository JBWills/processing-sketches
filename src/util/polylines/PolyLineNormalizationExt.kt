package util.polylines.polyLine

import coordinate.Segment
import util.DoubleRange
import util.numbers.boundMin
import util.polylines.PolyLine
import util.polylines.forEachSegment
import util.polylines.toPolyLine
import util.polylines.toSegments

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
  forEachSegment { segment ->
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
      newSegment.length < distRangeClipped.start -> newSegment
      else -> {
        val splitSegments: List<Segment> =
          newSegment.walk(distRangeClipped.endInclusive).toSegments()
        result.addAll(splitSegments.dropLast(1))

        val last = splitSegments.last()

        if (last.length in distRangeClipped) {
          result.add(last)
          null
        } else {
          last
        }
      }
    }
  }

  lastSegment?.let { result.add(it) }

  return result.toPolyLine()
}
