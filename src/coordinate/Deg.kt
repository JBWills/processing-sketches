package coordinate

import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.EitherDirection
import util.cos
import util.equalsDelta
import util.sin
import util.toRadians
import kotlin.math.abs
import kotlin.math.min

enum class RotationDirection {
  Clockwise,
  CounterClockwise,
  EitherDirection
}

fun lockValueTo360(v: Double) = (360 + (v % 360)) % 360

/**
 * Represents degrees from 0 to 360.
 *
 * On a 2d grid, assume 0 equals pointing right.
 */
class Deg(var value: Double) {
  companion object {
    val HORIZONTAL = Deg(0)
    val UP_45 = Deg(45)
    val DOWN_45 = Deg(135)
    val VERTICAL = Deg(90)
  }

  constructor(v: Number) : this(v.toDouble())

  init {
    value = lockValueTo360(value)
  }

  val rad get() = value.toRadians()

  val unitVector get() = Point(rad.cos(), -rad.sin())

  fun rotation(to: Deg, dir: RotationDirection = EitherDirection): Double {
    val start = if (dir == Clockwise) value else to.value
    val end = if (dir == Clockwise) to.value else value

    val diff = abs(end - start)

    return when {
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

  fun isHorizontal() = isParallelWith(180)
  fun isVertical() = isParallelWith(90)

  override fun toString(): String {
    return "Deg(value=$value)"
  }

  fun isParallelWith(other: Deg, relaxed: Boolean = true) = if (relaxed) {
    equalsRelaxed(other) || equalsRelaxed(other + Deg(180))
  } else {
    equals(other) || equals(other + Deg(180))
  }

  fun isParallelWith(other: Number, relaxed: Boolean = true) = isParallelWith(Deg(other), relaxed)
  fun isParallelWith(other: Line, relaxed: Boolean = true) = isParallelWith(other.slope, relaxed)

  fun equalsRelaxed(other: Deg): Boolean {
    return value.equalsDelta(other.value, 0.1)
  }

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