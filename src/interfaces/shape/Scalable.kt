package interfaces.shape

import coordinate.Point
import coordinate.Point3

interface Scalable<T> {
  fun scaled3(scale: Point3, anchor: Point3): T = scaled(scale.toPoint(), anchor.toPoint())
  fun scaled(scale: Point, anchor: Point): T
}
