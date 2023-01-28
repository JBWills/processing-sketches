package coordinate

import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.EitherDirection
import coordinate.RotationDirection.WhicheverLarger
import kotlinx.serialization.Serializable
import util.numbers.cos
import util.numbers.equalsDelta
import util.numbers.sin
import util.numbers.tan
import util.numbers.toRadians
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("unused")
enum class RotationDirection {
  Clockwise,
  CounterClockwise,
  EitherDirection,
  WhicheverLarger,
}

fun lockValueTo360(v: Double) = (360 + (v % 360)) % 360

/**
 * Represents degrees from 0 to 360.
 *
 * On a 2d grid, assume 0 equals pointing right. increasing values move counter-clockwise
 */
@Serializable
data class Deg(private val inputValue: Double) {

  val unboundValue get() = inputValue

  val value = lockValueTo360(inputValue)

  @Suppress("unused")
  companion object {
    const val Whole = 360.0
    const val Half = 180.0
    const val Quarter = 90.0
    val HORIZONTAL = Deg(0)
    val UP_45 = Deg(45)
    val DOWN_45 = Deg(135)
    val VERTICAL = Deg(Quarter)
  }

  constructor(v: Number) : this(v.toDouble())

  val rad get() = value.toRadians()

  val unitVector get() = Point(rad.cos(), -rad.sin())

  val perpendicular: Deg get() = plus(Quarter)

  fun rotatedTowards(amt: Deg, dir: RotationDirection = Clockwise): Deg {
    val signedAmt = if (dir == Clockwise) amt.value else -(amt.value)
    return Deg(value + signedAmt)
  }

  fun rotation(to: Deg, dir: RotationDirection = EitherDirection): Double {
    if (equals(to)) return 0.0
    val (start, end) =
      if (dir == Clockwise) value to to.value
      else to.value to value

    val diff = abs(end - start)

    return when {
      (dir == WhicheverLarger) -> max(diff, 360 - diff)
      (dir == EitherDirection) -> min(diff, 360 - diff)
      (end >= start) -> diff
      else -> 360 - diff
    }
  }

  operator fun plus(other: Deg) = Deg(value + other.value)
  operator fun plus(rotation: Number) = Deg(value + rotation.toDouble())

  operator fun minus(other: Deg) = this + -other
  operator fun minus(rotation: Number) = this + -rotation.toDouble()
  operator fun unaryMinus() = Deg(-value)
  operator fun unaryPlus() = Deg(+value)
  operator fun div(other: Deg) = Deg(value / other.value)
  operator fun div(other: Number) = Deg(value / other.toDouble())

  fun sin(): Double = rad.sin()
  fun cos(): Double = rad.cos()
  fun tangent(): Deg = rad.tan().let { tan -> if (tan.isNaN()) HORIZONTAL else Deg(tan) }

  fun isHorizontal() = isParallelWith(180)
  fun isVertical() = isParallelWith(90)

  override fun toString(): String = "Deg(value=$value)"

  fun isParallelWith(other: Deg, relaxed: Boolean = true) =
    if (relaxed) equalsRelaxed(other) || equalsRelaxed(other + Deg(180))
    else equals(other) || equals(other + Deg(180))

  fun isParallelWith(other: Number, relaxed: Boolean = true) = isParallelWith(Deg(other), relaxed)
  fun isParallelWith(other: Line, relaxed: Boolean = true) = isParallelWith(other.slope, relaxed)

  fun equalsRelaxed(other: Deg): Boolean =
    rotation(other, EitherDirection) < 0.1

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Deg

    if (!value.equalsDelta(other.value)) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
