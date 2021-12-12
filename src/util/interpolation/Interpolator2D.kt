package util.interpolation

import coordinate.Point
import kotlinx.serialization.Serializable
import smile.interpolation.CubicSplineInterpolation2D
import smile.interpolation.Interpolation2D
import util.iterators.mapArray
import util.polylines.PolyLine

private typealias GetValue = (x: Double, y: Double) -> Double
typealias MakeFunction2D = (arr: Array<DoubleArray>, distanceX: Double, distanceY: Double) -> GetValue


@Suppress("unused")
@Serializable
sealed class Interpolator2D(private val makeFunction: MakeFunction2D) : InterpolationFunction2D {
  private var function: GetValue? = null

  fun setData(
    arr: Array<DoubleArray>,
    distanceX: Double = DefaultAmplitudeSampleDist,
    distanceY: Double = DefaultAmplitudeSampleDist
  ) {
    function = makeFunction(arr, distanceX, distanceY)
  }

  override fun setData(p: List<PolyLine>) = setData(p.mapArray { it.map(Point::x).toDoubleArray() })


  override fun interpolate(x: Double, y: Double): Double =
    function?.invoke(x, y) ?: throw Exception("Can't call interpolate before calling setData!")
}

private fun smile2D(construct: (x1: DoubleArray, x2: DoubleArray, y: Array<DoubleArray>) -> Interpolation2D): MakeFunction2D =
  { arr: Array<DoubleArray>, distanceX: Double, distanceY: Double ->
    when {
      arr.isEmpty() || arr.first().isEmpty() -> {
        throw Exception("Array size must be at least one")
      }
      arr.size == 1 -> {
        { x, _ -> arr[0][x.toInt()] }
      }
      arr[0].size == 1 -> {
        { _, y -> arr[y.toInt()][0] }
      }
      else -> {
        construct(
          DoubleArray(arr.size) { i -> i * distanceY },
          DoubleArray(arr[0].size) { i -> i * distanceX },
          arr,
        )::interpolate
      }
    }
  }

class CubicSpline2D : Interpolator2D(smile2D(::CubicSplineInterpolation2D))

@Suppress("unused")
enum class Interpolator2DType(val create: () -> Interpolator2D) {
  CubicSpline2DType({ CubicSpline2D() }),
  ;
}
