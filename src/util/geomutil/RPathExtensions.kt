package util.geomutil

import coordinate.Point
import geomerativefork.src.RPath
import geomerativefork.src.RPoint
import util.polylines.PolyLine

fun RPoint.toPoint() = Point(x, y)
fun RPath.toPoints() = points.map(RPoint::toPoint)
fun List<RPath>.toPoints() = map(RPath::toPoints)
fun Array<RPath>.toPoints() = map(RPath::toPoints)

fun RPath.contains(p: Point) = contains(p.toRPoint())

fun RPath.toPolyLine(): PolyLine = points.map(RPoint::toPoint)

fun List<RPath>.toPolyLines(): List<PolyLine> = map(RPath::toPolyLine)
