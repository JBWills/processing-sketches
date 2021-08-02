package util.polylines.polyLine

import coordinate.Point
import coordinate.Point.Companion.plusIf
import coordinate.Segment
import util.polylines.mapBySegment

typealias PolyLine = List<Point>

fun List<Segment>.toPolyLine(): PolyLine = map { it.p1 }.plusIf(lastOrNull()?.p2)
fun PolyLine.toSegments(): List<Segment> =
  mapBySegment { it }

fun PolyLine.isClosed() = !isEmpty() && first() == last()
