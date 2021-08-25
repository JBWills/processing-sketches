package interfaces.shape

import coordinate.Deg
import coordinate.Point
import util.polylines.PolyLine

fun interface Rotatable<out T> {
  fun rotated(deg: Deg, anchor: Point): PolyLine
}
