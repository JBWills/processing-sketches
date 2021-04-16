package util.geomutil

import coordinate.Deg
import coordinate.Point
import geomerativefork.src.RShape

fun RShape.rotated(amt: Deg, around: Point): RShape {
  rotate(amt.value.toFloat(), around.toRPoint())
  return this
}

fun ellipse(center: Point, size: Point) = RShape.createEllipse(center.toRPoint(), size.toRPoint())
