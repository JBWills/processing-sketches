package interfaces.shape

import coordinate.ShapeTransform

interface Transformable<out T> : Scalable<T>, Translateable<T>

fun <T : Transformable<T>> T.transform(s: ShapeTransform): T = s.transform(this)
fun <T : Transformable<T>> List<T>.transform(s: ShapeTransform): List<T> = map { s.transform(it) }
fun <T : Transformable<T>> T.revertTransform(s: ShapeTransform): T = s.inverted().transform(this)
fun <T : Transformable<T>> List<T>.revertTransform(s: ShapeTransform): List<T> =
  map { it.revertTransform(s) }
