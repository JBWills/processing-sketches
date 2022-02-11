package util.polylines

import coordinate.Point
import coordinate.Point.Companion.plusIf
import coordinate.Segment
import coordinate.Segment.Companion.toSegment
import util.polylines.iterators.mapBySegment

typealias PolyLine = List<Point>

fun List<Segment>.toPolyLine(): PolyLine = map { it.p1 }.plusIf(lastOrNull()?.p2)
fun PolyLine.toSegments(): List<Segment> =
  mapBySegment { it }

fun PolyLine.isClosed() = !isEmpty() && first() == last()

fun PolyLine.toSegment(): Segment = (first() to last()).toSegment()
