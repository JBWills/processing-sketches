package util.interpolation

import coordinate.Point
import util.polylines.PolyLine

interface InterpolationFunction2D {
  fun setData(p: List<PolyLine>)
  fun interpolate(x: Double, y: Double): Double
  fun interpolate(p: Point): Double = interpolate(p.x, p.y)
}
