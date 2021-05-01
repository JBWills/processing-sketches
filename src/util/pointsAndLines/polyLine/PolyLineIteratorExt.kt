package util.pointsAndLines.polyLine

import coordinate.Point
import coordinate.Segment
import util.iterators.forEachWithNext
import util.iterators.mapWithNext

fun PolyLine.mapWithLength(block: (point: Point, length: Double) -> Point): PolyLine {
  if (isEmpty()) return emptyList()

  var length = 0.0
  var lastPoint = first()
  return map { point ->
    length += Segment(point, lastPoint).length
    val newPoint = block(point, length)
    lastPoint = point
    newPoint
  }
}

fun PolyLine.forEachSegment(block: (Segment) -> Unit) = forEachWithNext { curr, next ->
  next?.let { block(Segment(curr, it)) }
}

fun <R> PolyLine.mapBySegment(block: (Segment) -> R): List<R> =
  mapWithNext { curr, next -> block(Segment(curr, next)) }

fun PolyLine.flatMapSegments(block: (Segment) -> List<Segment>): List<Segment> =
  mapBySegment(block).flatten()
