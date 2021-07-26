package interfaces.shape

import coordinate.Point
import coordinate.Point3

interface Translateable<T> {
  fun translated(translate: Point): T
  fun translated3(translate: Point3): T = translated(translate.toPoint())
}
