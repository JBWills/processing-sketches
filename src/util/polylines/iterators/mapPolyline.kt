package util.polylines.iterators

import coordinate.Point
import coordinate.Segment
import util.polylines.PolyLine

fun <T> PolyLine.mapWithLength(block: (point: Point, length: Double) -> T): List<T> {
  if (isEmpty()) return listOf()

  var length = 0.0
  var lastPoint = first()
  return map { point ->
    length += Segment(point, lastPoint).length
    val newPoint = block(point, length)
    lastPoint = point
    newPoint
  }
}
