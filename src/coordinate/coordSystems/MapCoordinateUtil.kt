package coordinate.coordSystems

import coordinate.BoundRect
import coordinate.Point
import coordinate.transforms.ScaleTransform
import coordinate.transforms.ShapeTransformGroup
import coordinate.transforms.TranslateTransform

fun getCoordinateMap(from: BoundRect, to: BoundRect): ShapeTransformGroup =
  ShapeTransformGroup(
    ScaleTransform(Point(to.size.x / from.size.x), from.center),
    TranslateTransform(to.center - from.center),
  )

