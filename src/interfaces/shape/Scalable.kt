package interfaces.shape

import coordinate.Point

fun interface Scalable<T> {
  fun scaled(scale: Point, anchor: Point): T
}
