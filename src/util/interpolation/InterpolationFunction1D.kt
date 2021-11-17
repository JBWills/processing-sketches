package util.interpolation

import util.polylines.PolyLine

interface InterpolationFunction1D {
  fun setData(p: PolyLine)
  fun interpolate(x: Double): Double
}
