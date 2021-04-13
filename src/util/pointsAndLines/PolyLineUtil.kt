package util.pointsAndLines

import coordinate.BoundRect
import coordinate.ContinuousMaskedShape
import coordinate.Point
import coordinate.Segment
import util.iterators.forEachWithNext
import util.iterators.forEachWithSurroundingCyclical

typealias PolyLine = List<Point>

fun PolyLine.forEachSegment(block: (Segment) -> Unit) = when {
  isClosed() -> forEachWithSurroundingCyclical { _, curr, next ->
    block(Segment(curr, next))
  }
  else -> forEachWithNext { curr, next ->
    next?.let { block(Segment(curr, it)) }
  }
}

typealias MutablePolyLine = MutableList<Point>

fun PolyLine.isClosed() = !isEmpty() && first() == last()

fun PolyLine.bound(bound: BoundRect): List<PolyLine> =
  ContinuousMaskedShape(this, bound).toBoundPoints(true)

@JvmName("boundLines")
fun List<PolyLine>.bound(bound: BoundRect): List<PolyLine> = flatMap {
  it.bound(bound)
}

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

@JvmName("ShiftSegments")
fun List<Segment>.shifted(p: Point) = map { it + p }

fun PolyLine.shifted(p: Point): PolyLine = map { it + p }
fun MutablePolyLine.shiftInPlace(p: Point): Unit = indices.forEach { this[it] += p }
