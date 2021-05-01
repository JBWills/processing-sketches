package util.pointsAndLines.polyLine

import coordinate.Point
import coordinate.Point.Companion.plusIf
import coordinate.Segment


typealias PolyLine = List<Point>

fun List<Segment>.toPolyLine(): PolyLine = map { it.p1 }.plusIf(lastOrNull()?.p2)
fun PolyLine.toSegments(): List<Segment> =
  mapBySegment { it }

fun PolyLine.isClosed() = !isEmpty() && first() == last()
