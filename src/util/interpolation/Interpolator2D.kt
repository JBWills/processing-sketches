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

typealias InterpolatorFunction2D = (Double) -> Double
typealias MakeFunction2D = (x: DoubleArray, y: DoubleArray) -> InterpolatorFunction2D


@Suppress("unused")
@Serializable
enum class Interpolator2D(val makeFunction: MakeFunction2D) {
  CubicSpline2D(smile2D(::CubicSplineInterpolation1D)),
  Kriging2D(smile2D(::KrigingInterpolation1D)),
  Linear2D(smile2D(::LinearInterpolation)),
  Shepard2D(smile2D(::ShepardInterpolation1D)),
  RbfGaussian2D(rbf(GaussianRadialBasis())),
  RbfMultiQuadratic2D(rbf(MultiquadricRadialBasis())),
  RbfInverseMultiQuadratic2D(rbf(InverseMultiquadricRadialBasis())),
  RbfThinPlate2D(rbf(ThinPlateRadialBasis())),
  ;

  var x: DoubleArray? = null
  var y: DoubleArray? = null
  private var xLength: Double? = null
  val function: InterpolatorFunction2D by lazy {
    (x to y).letNonNull { xNotNull, yNotNull ->
      makeFunction(xNotNull, yNotNull)
    } ?: throw Exception("Data not initialized with setData() yet.")
  }

  fun setData(
    x: DoubleArray,
    y: DoubleArray,
  ) {
    this.x = x
    this.y = y
  }

  fun interpolate(x: Double) = function(x)
}

private fun smile2D(construct: (x: DoubleArray, y: DoubleArray) -> Interpolation): MakeFunction2D =
  { x: DoubleArray, y: DoubleArray ->
    val interpolation = construct(x, y);
    { x2 -> interpolation.interpolate(x2) }
  }

private fun rbf(rbf: RadialBasisFunction) = smile2D { x, y -> RBFInterpolation1D(x, y, rbf) }
