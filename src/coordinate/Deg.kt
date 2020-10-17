package coordinate

import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.EitherDirection
import util.cos
import util.sin
import util.toRadians
import kotlin.math.abs
import kotlin.math.min

enum class RotationDirection {
  Clockwise,
  CounterClockwise,
  EitherDirection
}

fun lockValueTo360(v: Float) = (360 + (v % 360)) % 360

/**
 * Represents degrees from 0 to 360.
 *
 * On a 2d grid, assume 0 equals pointing right.
 */
class Deg(var value: Float) {
  companion object {
    val HORIZONTAL = Deg(0f)
    val UP_45 = Deg(45f)
    val DOWN_45 = Deg(135f)
    val VERTICAL = Deg(90f)
  }

  constructor(v: Int) : this(v.toFloat())

  init {
    value = lockValueTo360(value)
  }

  val rad get() = value.toRadians()

  val unitVector get() = Point(rad.cos(), -rad.sin())

  fun rotation(to: Deg, dir: RotationDirection = EitherDirection): Float {
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
  operator fun plus(rotation: Number) = Deg(value + rotation.toFloat())

  operator fun minus(other: Deg) = this + -other
  operator fun minus(rotation: Number) = this + -rotation.toFloat()
  operator fun unaryMinus() = Deg(-value)
  operator fun unaryPlus() = Deg(+value)
  operator fun div(other: Deg) = Deg(value / other.value)
  operator fun div(other: Number) = Deg(value / other.toFloat())

  fun isHorizontal() = value % 180f == 0f
  fun isVertical() = value % 180f != 0f && value % 90f == 0f

  override fun toString(): String {
    return "Deg(value=$value)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Deg

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}