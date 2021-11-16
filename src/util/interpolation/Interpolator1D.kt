package util.interpolation

import kotlinx.serialization.Serializable
import smile.interpolation.CubicSplineInterpolation1D
import smile.interpolation.Interpolation
import smile.interpolation.KrigingInterpolation1D
import smile.interpolation.LinearInterpolation
import smile.interpolation.RBFInterpolation1D
import smile.interpolation.ShepardInterpolation1D
import smile.math.rbf.GaussianRadialBasis
import smile.math.rbf.InverseMultiquadricRadialBasis
import smile.math.rbf.MultiquadricRadialBasis
import smile.math.rbf.RadialBasisFunction
import smile.math.rbf.ThinPlateRadialBasis
import util.base.letNonNull


typealias InterpolatorFunction1D = (Double) -> Double
typealias MakeFunction1D = (arr: DoubleArray, distanceX: Double) -> InterpolatorFunction1D


@Suppress("unused")
@Serializable
enum class Interpolator1D(val makeFunction: MakeFunction1D) {
  CubicSpline1D(smile1D(::CubicSplineInterpolation1D)),
  Kriging1D(smile1D(::KrigingInterpolation1D)),
  Linear1D(smile1D(::LinearInterpolation)),
  Shepard1D(smile1D(::ShepardInterpolation1D)),
  RbfGaussian1D(rbf(GaussianRadialBasis())),
  RbfMultiQuadratic1D(rbf(MultiquadricRadialBasis())),
  RbfInverseMultiQuadratic1D(rbf(InverseMultiquadricRadialBasis())),
  RbfThinPlate1D(rbf(ThinPlateRadialBasis())),
  ;

  var data: DoubleArray? = null
  private var xLength: Double? = null
  val function: InterpolatorFunction1D by lazy {
    (data to xLength).letNonNull { dataNonNull, lengthNonNull ->
      makeFunction(dataNonNull, lengthNonNull)
    } ?: throw Exception("Data not initialized with setData() yet.")
  }

  fun setData(
    arr: DoubleArray,
    distanceX: Double = DefaultAmplitudeSampleDist
  ) {
    data = arr
    xLength = distanceX
  }

  fun interpolate(x: Double) = function(x)
}

private fun smile1D(construct: (x: DoubleArray, y: DoubleArray) -> Interpolation): MakeFunction1D =
  { arr: DoubleArray, xLength: Double ->

    val (xValues, yValues) = amplitudeToPoints(arr, xLength)
    val interpolation = construct(xValues, yValues);
    { x -> interpolation.interpolate(x) }
  }

private fun rbf(rbf: RadialBasisFunction) = smile1D { x, y -> RBFInterpolation1D(x, y, rbf) }
