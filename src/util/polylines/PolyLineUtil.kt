package util.polylines

import coordinate.BoundRect
import coordinate.Point
import coordinate.Point.Companion.maxXY
import coordinate.Point.Companion.minXY
import coordinate.Segment
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import util.iterators.mapWithSibling
import util.polylines.clipping.ForceClosedOption
import util.polylines.clipping.ForceClosedOption.Default
import util.polylines.clipping.clip

typealias MinMaxPoints = Pair<Point, Point>

val DefaultMinMax: MinMaxPoints = Point.MAX_VALUE to Point.MIN_VALUE

val PolyLine.length: Double
  get() = mapWithSibling { curr, next -> curr.dist(next) }.sum()

fun PolyLine.bound(bound: BoundRect): List<PolyLine> = listOf(this).bound(bound)

fun PolyLine.closed() = when {
  isEmpty() || isClosed() -> this
  else -> this + first()
}

fun MutablePolyLine.closedInPlace(): MutablePolyLine = apply {
  if (!isEmpty() && !isClosed()) this.add(first())
}

@JvmName("boundLines")
fun List<PolyLine>.bound(
  bound: BoundRect,
  forceClosedOption: ForceClosedOption = Default
): List<PolyLine> = clip(bound.toPolyLine(), INTERSECTION, forceClosedOption)

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

private fun MinMaxPoints.toBounds() =
  if (equals(DefaultMinMax)) BoundRect(Point.Zero, Point.Zero)
  else BoundRect(first, second)

val Iterable<Point>.bounds: BoundRect get() = minMax.toBounds()

val Iterable<PolyLine>.boundsAll: BoundRect get() = minMaxAll.toBounds()

val Iterable<Point>.minMax: MinMaxPoints
  get() = fold(initial = DefaultMinMax) { (min, max), value ->
    minXY(min, value) to maxXY(max, value)
  }

val Iterable<Iterable<Point>>.minMaxAll: MinMaxPoints
  get() = fold(initial = DefaultMinMax) { (min, max), line ->
    val (lineMin, lineMax) = line.minMax
    minXY(min, lineMin) to maxXY(max, lineMax)
  }
