package util.algorithms.contouring

import coordinate.Point
import coordinate.Segment
import util.iterators.get
import util.polylines.attach
import util.polylines.polyLine.PolyLine
import util.polylines.polyLine.isClosed

/**
 * Merge an unordered list of segments into a set of polylines.
 */
fun List<Segment>.mergeSegments(): Set<PolyLine> {
  val segmentsLeft = this.toMutableSet()
  val segmentsByEndpoint = mutableMapOf<Point, MutableSet<Segment>>().apply {
    this@mergeSegments.forEach { segment ->
      segment.points.forEach { endPoint ->
        if (!containsKey(endPoint)) put(endPoint, mutableSetOf())
        getValue(endPoint).add(segment)
      }
    }
  }

  fun markSegmentProcessed(s: Segment) {
    segmentsLeft.remove(s)
    s.points.forEach { point ->
      segmentsByEndpoint.getOrDefault(point, mutableSetOf()).remove(s)
      if (segmentsByEndpoint[point]?.size == 0) {
        segmentsByEndpoint.remove(point)
      }
    }
  }

  val result = mutableSetOf<PolyLine>()
  var currLine = mutableListOf<Point>()

  fun completeCurrLine() {
    result.add(currLine)
    currLine = mutableListOf()
  }

  fun attachToCurrLinePoint(p: Point) {
    val segment = segmentsByEndpoint.getValue(p).get()
    currLine.attach(segment)
    markSegmentProcessed(segment)
  }

  while (segmentsLeft.isNotEmpty()) {
    if (currLine.isEmpty()) segmentsLeft.get().let { segment ->
      currLine = segment.points.toMutableList()
      markSegmentProcessed(segment)
    }

    when {
      currLine.isClosed() -> completeCurrLine()
      currLine.last() in segmentsByEndpoint -> attachToCurrLinePoint(currLine.last())
      currLine.first() in segmentsByEndpoint -> attachToCurrLinePoint(currLine.first())
      else -> completeCurrLine()
    }
  }

  if (currLine.isNotEmpty()) result.add(currLine)

  return result
}
