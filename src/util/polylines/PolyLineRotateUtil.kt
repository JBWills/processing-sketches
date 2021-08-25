package util.polylines

import coordinate.Deg
import coordinate.Point
import coordinate.transforms.applyTo
import coordinate.transforms.rotationTransform

fun PolyLine.rotate(deg: Deg, anchor: Point) =
  rotationTransform(deg, anchor).applyTo(this)
