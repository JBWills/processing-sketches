package util.polylines

import coordinate.BoundRect
import coordinate.Point
import coordinate.Point.Companion.maxXY
import coordinate.Point.Companion.minXY
import coordinate.Segment
import util.iterators.mapWithNext
import util.polylines.clipping.clipperIntersection

val PolyLine.length: Double
  get() = mapWithNext { curr, next -> curr.dist(next) }.sum()

fun PolyLine.bound(bound: BoundRect): List<PolyLine> = listOf(this).bound(bound)

fun PolyLine.closed() = when {
  isEmpty() || isClosed() -> this
  else -> this + first()
}

@JvmName("boundLines")
fun List<PolyLine>.bound(bound: BoundRect): List<PolyLine> = clipperIntersection(bound.toPolyLine())

@JvmName("ShiftSegments")
fun List<Segment>.translated(p: Point) = map { it + p }

@JvmName("translatedLine")
fun PolyLine.translated(p: Point): PolyLine = map { it + p }

fun List<PolyLine>.translated(p: Point): List<PolyLine> = map { it.translated(p) }

fun MutablePolyLine.translatedInPlace(p: Point): Unit = indices.forEach { this[it] += p }

fun MutablePolyLine.appendSegmentOrStartNewLine(s: Segment): MutablePolyLine? = when {
  isEmpty() -> {
    add(s.p1)
    add(s.p2)
    null
  }
  last() == s.p1 -> {
    add(s.p2)
    null
  }
  else -> mutableListOf(s.p1, s.p2)
}

fun PolyLine.connectWith(other: PolyLine): PolyLine = when {
  last() == other.first() -> this + other
  last() == other.last() -> this + other.reversed()
  first() == other.first() -> other.reversed() + this
  else -> other + this
}

val Iterable<Point>.bounds: BoundRect
  get() = minMax.let { (topLeft, bottomRight) -> BoundRect(topLeft, bottomRight) }

val Iterable<Point>.minMax: Pair<Point, Point>
  get() = fold(initial = Point.MAX_VALUE to Point.MIN_VALUE) { (min, max), value ->
    minXY(min, value) to maxXY(max, value)
  }
