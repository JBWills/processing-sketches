package util.geomutil

import coordinate.Point
import geomerativefork.src.RShape
import util.mapArray

fun List<Point>.toRShape() = RShape(mapArray { it.toRPoint() })
