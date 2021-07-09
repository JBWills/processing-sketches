package interfaces.shape

import coordinate.ShapeTransform

interface Transformable<T> : Scalable<T>, Translateable<T>

fun <T : Transformable<T>> T.transform(s: ShapeTransform): T = s.transform(this)
fun <T : Transformable<T>> T.revertTransform(s: ShapeTransform): T = s.inverted().transform(this)
