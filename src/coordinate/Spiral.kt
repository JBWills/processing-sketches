package coordinate

import util.DoubleRange

fun getSpiralPointFunc(
  originFunc: (Double, Double, Deg) -> Point,
  lengthFunc: (Double, Double, Deg) -> Double,
): (Double, Double) -> Point = { t, percentAlong ->

  val degrees = Deg(t * 360.0)

  originFunc(t, percentAlong, degrees) + degrees.unitVector * lengthFunc(t, percentAlong, degrees)
}

class Spiral(
  originFunc: (Double, Double, Deg) -> Point,
  lengthFunc: (Double, Double, Deg) -> Double,
  rotationsRange: DoubleRange,
) : FShape(getSpiralPointFunc(originFunc, lengthFunc), rotationsRange) {
  companion object {
  }
}