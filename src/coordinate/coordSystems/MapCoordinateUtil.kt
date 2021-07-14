package coordinate.coordSystems

import coordinate.BoundRect
import coordinate.Point
import coordinate.ScaleTransform
import coordinate.ShapeTransformGroup
import coordinate.TranslateTransform

fun getTransformToMapCoord(from: BoundRect, to: BoundRect): ShapeTransformGroup =
  ShapeTransformGroup(
    ScaleTransform(Point(to.size.x / from.size.x), from.center),
    TranslateTransform(to.center),
  )

