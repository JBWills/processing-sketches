package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.Scalar

val Mat.size: Point get() = Point(cols(), rows())
val Mat.bounds: BoundRect get() = BoundRect(Point.Zero, size - 1)


fun Mat.put(bytes: ByteArray, offset: Point = Point.Zero) = put(offset.yi, offset.xi, bytes)

fun Mat.putFloat(p: Point, s: Scalar): Boolean {
  if (!bounds.contains(p)) return false
  put(p.yi, p.xi, s.toFloatArray())
  return true
}

fun Mat.put(p: Point, d: Double) = put(p.yi, p.xi, *Scalar(d).values)
fun Mat.maybePut(p: Point, d: Double) {
  if (contains(p)) {
    put(p, d)
  }
}
