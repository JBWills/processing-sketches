package util.interpolation

import smile.interpolation.CubicSplineInterpolation1D
import smile.interpolation.Interpolation
import smile.interpolation.KrigingInterpolation1D
import smile.interpolation.LinearInterpolation
import smile.interpolation.RBFInterpolation1D
import smile.interpolation.ShepardInterpolation1D
import smile.math.rbf.RadialBasisFunction

sealed class InterpolatorType2D(
  val dataX: DoubleArray,
  val dataY: DoubleArray,
) {
  val function: InterpolatorFunction1D by lazy { create(dataX, dataY) }

  abstract fun create(x: DoubleArray, y: DoubleArray): InterpolatorFunction1D

  fun interpolate(x: Double) = function(x)
}

sealed class Smile2D(
  x: DoubleArray,
  y: DoubleArray,
  val construct: (x: DoubleArray, y: DoubleArray) -> Interpolation,
) : InterpolatorType2D(x, y) {
  final override fun create(x: DoubleArray, y: DoubleArray): InterpolatorFunction1D =
    construct(x, y)::interpolate
}

class CubicSpline2D(
  x: DoubleArray,
  y: DoubleArray,
) : Smile2D(x, y, ::CubicSplineInterpolation1D)

class Kriging2D(
  x: DoubleArray,
  y: DoubleArray,
) : Smile2D(x, y, ::KrigingInterpolation1D)

class Linear2D(
  x: DoubleArray,
  y: DoubleArray,
) : Smile2D(x, y, ::LinearInterpolation)

class Rbf2D(
  x: DoubleArray,
  y: DoubleArray,
  radialBasisFn: RadialBasisFunction,
) : Smile2D(x, y, { x, y -> RBFInterpolation1D(x, y, radialBasisFn) })

class Shepard2D(
  x: DoubleArray,
  y: DoubleArray,
) : Smile2D(x, y, ::ShepardInterpolation1D)
