package util.polylines

import coordinate.Point
import coordinate.Segment
import util.polylines.polyLine.isClosed

typealias  MutablePolyLine = MutableList<Point>

fun MutablePolyLine.canAttach(s: Segment): Boolean = canAttachStart(s) || canAttachEnd(s)

fun MutablePolyLine.canAttachEnd(s: Segment): Boolean = !isClosed() && (isEmpty() ||
  (last() == s.p1 || last() == s.p2))

fun MutablePolyLine.canAttachStart(s: Segment): Boolean = !isClosed() && (isEmpty() ||
  (first() == s.p1 || first() == s.p2))

fun MutablePolyLine.attach(s: Segment) = when {
  canAttachEnd(s) -> attachEnd(s)
  canAttachStart(s) -> attachStart(s)
  else -> throw Exception("Trying to attach to polyline when you cant!")
}

fun MutablePolyLine.attachEnd(s: Segment) {
  when {
    !canAttachEnd(s) -> throw Exception("Trying to attach to end of polyline when you cant!")
    isEmpty() -> addAll(s.points)
    last() == s.p1 -> add(s.p2)
    last() == s.p2 -> add(s.p1)
  }
}

fun MutablePolyLine.attachStart(s: Segment) {
  when {
    !canAttachStart(s) -> throw Exception("Trying to attach to start of polyline when you cant!")
    isEmpty() -> addAll(s.points)
    first() == s.p1 -> add(0, s.p2)
    first() == s.p2 -> add(0, s.p1)
  }
}

/**
 * Add points to the line, but skip the first point if it's the same as the last point
 */
fun MutablePolyLine.addPoints(vararg points: Point) {
  if (points.isEmpty()) return
  if (lastOrNull() == points.first()) {
    addAll(points.sliceArray(1 until points.size))
  } else {
    addAll(points)
  }
}
