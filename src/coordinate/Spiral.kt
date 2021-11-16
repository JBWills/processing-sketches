package coordinate

import util.PointRange
import util.atAmountAlong
import util.base.DoubleRange

fun getSpiralPointFunc(
  originFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Point,
  lengthFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Double,
): (Double, Double) -> Point = { t, percentAlong ->
  val degrees = Deg(t * 360.0)

  val newOrigin = originFunc(t, percentAlong, degrees)
  val newLength = lengthFunc(t, percentAlong, degrees)

  newOrigin + degrees.unitVector * newLength
}

class Spiral(
  originFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Point,
  lengthFunc: (t: Double, percentAlong: Double, degrees: Deg) -> Double,
  rotationsRange: DoubleRange,
) : FShape(getSpiralPointFunc(originFunc, lengthFunc), rotationsRange)

class PointSpiral(
  origin: Point,
  sizeRange: PointRange,
  rotationsRange: DoubleRange,
  f: (t: Double, percentAlong: Double, degrees: Deg, spiralPoint: Point) -> Point,
) : FShape(
  { t, percentAlong ->
    val degrees = Deg(t * 360.0)
    val degSinCos = Point(degrees.cos(), degrees.sin())
    val sizeXY = sizeRange.atAmountAlong(percentAlong)

    f(
      t,
      percentAlong,
      degrees,
      origin + degrees.unitVector * (sizeXY * degSinCos).magnitude,
    )
  },
  rotationsRange,
)
