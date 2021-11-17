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
import util.polylines.PolyLine

typealias GetYAAtX = (Double) -> Double
typealias MakeFunction1D = (arr: DoubleArray, distanceX: Double) -> GetYAAtX


@Suppress("unused")
@Serializable
enum class Interpolator1D(private val makeFunction: MakeFunction1D) : InterpolationFunction1D {
  CubicSpline1D(smile1D(::CubicSplineInterpolation1D)),
  Kriging1D(smile1D(::KrigingInterpolation1D)),
  Linear1D(smile1D(::LinearInterpolation)),
  Shepard1D(smile1D(::ShepardInterpolation1D)),
  RbfGaussian1D(rbf(GaussianRadialBasis())),
  RbfMultiQuadratic1D(rbf(MultiquadricRadialBasis())),
  RbfInverseMultiQuadratic1D(rbf(InverseMultiquadricRadialBasis())),
  RbfThinPlate1D(rbf(ThinPlateRadialBasis())),
  ;

  private var function: GetYAAtX? = null

  fun setData(arr: DoubleArray, distanceX: Double = DefaultAmplitudeSampleDist) {
    function = makeFunction(arr, distanceX)
  }

  override fun setData(p: PolyLine) = setData(p, DefaultAmplitudeSampleDist)

  @Suppress("MemberVisibilityCanBePrivate")
  fun setData(points: PolyLine, distanceX: Double) =
    setData(DoubleArray(points.size) { i -> points[i].y }, distanceX)

  override fun interpolate(x: Double) =
    function?.invoke(x) ?: throw Exception("Can't call interpolate before calling setdata!")
}

private fun smile1D(construct: (x: DoubleArray, y: DoubleArray) -> Interpolation): MakeFunction1D =
  { arr: DoubleArray, xLength: Double ->

    val (xValues, yValues) = amplitudeToPoints(arr, xLength)
    val interpolation = construct(xValues, yValues);
    { x -> interpolation.interpolate(x) }
  }

private fun rbf(rbf: RadialBasisFunction) = smile1D { x, y -> RBFInterpolation1D(x, y, rbf) }
