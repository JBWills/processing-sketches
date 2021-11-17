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
import util.interpolation.GetYAAtX
import util.interpolation.InterpolationFunction1D
import util.polylines.PolyLine

typealias MakeFunction2D = (x: DoubleArray, y: DoubleArray) -> GetYAAtX

@Suppress("unused")
@Serializable
enum class PointInterpolator1D(private val makeFunction: MakeFunction2D) : InterpolationFunction1D {
  CubicSpline2D(pointSmile(::CubicSplineInterpolation1D)),
  Kriging2D(pointSmile(::KrigingInterpolation1D)),
  Linear2D(pointSmile(::LinearInterpolation)),
  Shepard2D(pointSmile(::ShepardInterpolation1D)),
  RbfGaussian2D(pointRbf(GaussianRadialBasis())),
  RbfMultiQuadratic2D(pointRbf(MultiquadricRadialBasis())),
  RbfInverseMultiQuadratic2D(pointRbf(InverseMultiquadricRadialBasis())),
  RbfThinPlate2D(pointRbf(ThinPlateRadialBasis())),
  ;

  private var function: GetYAAtX? = null

  @Suppress("MemberVisibilityCanBePrivate")
  fun setData(x: DoubleArray, y: DoubleArray) {
    function = makeFunction(x, y)
  }

  override fun setData(p: PolyLine) {
    val x = DoubleArray(p.size)
    val y = DoubleArray(p.size)

    p.forEachIndexed { index, point ->
      x[index] = point.x
      y[index] = point.y
    }

    setData(x, y)
  }

  override fun interpolate(x: Double) =
    function?.invoke(x) ?: throw Exception("Can't call interpolate before calling setdata!")
}

private fun pointSmile(construct: (x: DoubleArray, y: DoubleArray) -> Interpolation): MakeFunction2D =
  { x: DoubleArray, y: DoubleArray ->
    val interpolation = construct(x, y);
    { x2 -> interpolation.interpolate(x2) }
  }

private fun pointRbf(rbf: RadialBasisFunction) =
  pointSmile { x, y -> RBFInterpolation1D(x, y, rbf) }
