package coordinate

import util.DoubleRange

fun getSpiralPointFunc(
  originFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Point,
  lengthFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Double,
): (Double, Double) -> Point = { t, percentAlong ->
  val degrees = Deg(t * 360.0)

  originFunc(t, percentAlong, degrees) + degrees.unitVector * lengthFunc(t, percentAlong, degrees)
}

class Spiral(
  originFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Point,
  lengthFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Double,
  rotationsRange: DoubleRange,
) : FShape(getSpiralPointFunc(originFunc, lengthFunc), rotationsRange)
