package util.geomutil

import coordinate.Point
import geomerativefork.src.RPath
import geomerativefork.src.RPoint

fun RPoint.toPoint() = Point(x, y)
fun RPath.toPoints() = points.map(RPoint::toPoint)
fun List<RPath>.toPoints() = map(RPath::toPoints)
fun Array<RPath>.toPoints() = map(RPath::toPoints)
