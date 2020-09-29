package coordinate

import util.cos
import util.degreesToRadians
import util.sin
import kotlin.math.abs
import kotlin.math.min

fun lockValueTo360(v: Int) = (360 + (v % 360)) % 360

/**
 * Represents degrees from 0 to 360.
 *
 * On a 2d grid, assume 0 equals pointing right.
 */
class Deg(var value: Int) {
  companion object {
    val HORIZONTAL = Deg(0)
    val UP_45 = Deg(45)
    val DOWN_45 = Deg(135)
    val VERTICAL = Deg(90)
  }

  init {
    value = lockValueTo360(value)
  }

  val rad get() = value.degreesToRadians()

  val unitVector get() = Point(rad.cos(), -rad.sin())

  fun rotation(other: Deg): Int {
    val diff = abs(value - other.value)
    return min(360 - diff, diff)
  }

  operator fun plus(other: Deg) = Deg(value + other.value)
  operator fun plus(rotation: Int) = Deg(value + rotation)

  operator fun minus(other: Deg) = this + -other
  operator fun minus(rotation: Int) = this + -rotation
  operator fun unaryMinus() = Deg(-value)
  operator fun unaryPlus() = Deg(+value)

  fun isHorizontal() = value % 180 == 0
  fun isVertical() = value % 180 != 0 && value % 90 == 0

  override fun toString(): String {
    return "Deg(value=$value)"
  }
}