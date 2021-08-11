package interfaces.shape

import coordinate.Point

fun interface Scalable<out T> {
  fun scaled(scale: Point, anchor: Point): T
}
