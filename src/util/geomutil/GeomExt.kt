package util.geomutil

import coordinate.Point
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import util.mapArray

fun List<Point>.toRShape() = RShape(mapArray { it.toRPoint() })
fun List<Point>.toRPath() = RPath(mapArray { it.toRPoint() })
