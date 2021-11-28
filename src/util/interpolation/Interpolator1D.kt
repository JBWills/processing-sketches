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
sealed class Interpolator1D(private val makeFunction: MakeFunction1D) : InterpolationFunction1D {
  private var function: GetYAAtX? = null

  fun setData(arr: DoubleArray, distanceX: Double = DefaultAmplitudeSampleDist) {
    function = makeFunction(arr, distanceX)
  }

  override fun setData(p: PolyLine) = setData(p, DefaultAmplitudeSampleDist)

  @Suppress("MemberVisibilityCanBePrivate")
  fun setData(points: PolyLine, distanceX: Double) =
    setData(DoubleArray(points.size) { i -> points[i].y }, distanceX)

  override fun interpolate(x: Double) =
    function?.invoke(x) ?: throw Exception("Can't call interpolate before calling setData!")
}

private fun smile1D(construct: (x: DoubleArray, y: DoubleArray) -> Interpolation): MakeFunction1D =
  { arr: DoubleArray, xLength: Double ->

    val (xValues, yValues) = amplitudeToPoints(arr, xLength)
    if (arr.isEmpty()) {
      throw Exception("Array size must be at least one")
    } else if (arr.size < 2) {
      { _ -> arr.first() }
    } else {
      val interpolation = construct(xValues, yValues);
      { x -> interpolation.interpolate(x) }
    }
  }

private fun rbf(rbf: RadialBasisFunction) = smile1D { x, y -> RBFInterpolation1D(x, y, rbf) }

class CubicSpline1D : Interpolator1D(smile1D(::CubicSplineInterpolation1D))
class Kriging1D : Interpolator1D(smile1D(::KrigingInterpolation1D))
class Linear1D : Interpolator1D(smile1D(::LinearInterpolation))
class Shepard1D : Interpolator1D(smile1D(::ShepardInterpolation1D))
class RbfGaussian1D : Interpolator1D(rbf(GaussianRadialBasis()))
class RbfMultiQuadratic1D : Interpolator1D(rbf(MultiquadricRadialBasis()))
class RbfInverseMultiQuadratic1D : Interpolator1D(rbf(InverseMultiquadricRadialBasis()))
class RbfThinPlate1D : Interpolator1D(rbf(ThinPlateRadialBasis()))

@Suppress("unused")
enum class Interpolator1DType(val create: () -> Interpolator1D) {
  CubicSpline1DType({ CubicSpline1D() }),
  Kriging1DType({ Kriging1D() }),
  Linear1DType({ Linear1D() }),
  Shepard1DType({ Shepard1D() }),
  RbfGaussian1DType({ RbfGaussian1D() }),
  RbfMultiQuadratic1DType({ RbfMultiQuadratic1D() }),
  RbfInverseMultiQuadratic1DType({ RbfInverseMultiQuadratic1D() }),
  RbfThinPlate1DType({ RbfThinPlate1D() }),
  ;
}
