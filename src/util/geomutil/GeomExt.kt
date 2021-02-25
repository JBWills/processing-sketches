package util.geomutil

import coordinate.Point
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import util.mapArray

fun List<Point>.toRShape() = RShape(mapArray { it.toRPoint() })
fun List<Point>.toRPath(closed: Boolean = false) = RPath(mapArray { it.toRPoint() })
  .apply { if (closed) addClose() }
