package interfaces.shape

import coordinate.Point

fun interface Translateable<T> {
  fun translated(translate: Point): T
}
