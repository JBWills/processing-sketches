package util.geomutil

import geomerativefork.src.RPath
import geomerativefork.src.RShape
import util.iterators.mapArray
import util.polylines.PolyLine

fun PolyLine.toRShape() = RShape(mapArray { it.toRPoint() })
fun PolyLine.toRPath(closed: Boolean = false) = RPath(mapArray { it.toRPoint() })
  .apply { if (closed) addClose() }

fun RShape.toPolyLines(): List<PolyLine> =
  paths.map { it.toPoints() }
