package interfaces.shape

import coordinate.Point

fun interface Translateable<out T> {
  fun translated(translate: Point): T
}
