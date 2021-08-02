package util.pointsAndLines.polyLine

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.ContinuousMaskedShape
import coordinate.Point
import coordinate.Point.Companion.maxXY
import coordinate.Point.Companion.minXY
import coordinate.Segment
import org.opencv.core.MatOfPoint
import util.mean
import util.pointsAndLines.mutablePolyLine.MutablePolyLine

private val _bound = { list: PolyLine, bound: BoundRect ->
  ContinuousMaskedShape(list, bound).toBoundPoints(true)
}.memoize()

private val _translatedSegments = { list: List<Segment>, p: Point ->
  list.map { it + p }
}.memoize()

private val _translated = { list: List<Point>, p: Point ->
  list.map { it + p }
}.memoize()

fun PolyLine.bound(bound: BoundRect): List<PolyLine> = _bound(this, bound)

fun PolyLine.closed(maxDistance: Double = Double.MAX_VALUE) = when {
  isEmpty() || isClosed() -> this
  else -> this + first().copy()
}

@JvmName("boundLines")
fun List<PolyLine>.bound(bound: BoundRect): List<PolyLine> = flatMap { _bound(it, bound) }

@JvmName("ShiftSegments")
fun List<Segment>.translated(p: Point) = _translatedSegments(this, p)

@JvmName("translatedLine")
fun PolyLine.translated(p: Point): PolyLine = _translated(this, p)

fun List<PolyLine>.translated(p: Point): List<PolyLine> = map { _translated(it, p) }

fun PolyLine.expandEndpointsToMakeMask(
  newBottom: Double = (maxByOrNull { it.y }?.y ?: firstOrNull()?.y ?: 0.0)
): PolyLine {
  if (size < 2) return this
  val start = first()
  val end = last()

  val midpoint = start.x + (end.x - start.x) / 2

  return Point(start.x, newBottom) + this + Point(end.x, newBottom)
}

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

fun MatOfPoint.toPolyLine(): PolyLine =
  toArray()
    .map { Point(it.x, it.y) }
    .closed(maxDistance = mean(rows(), cols()) / 10)

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

fun List<PolyLine>.toMatOfPointList() = map { it.toMatOfPoint() }

fun PolyLine.toMatOfPoint(): MatOfPoint =
  MatOfPoint().also { it.fromList(map(Point::toOpenCvPoint)) }
