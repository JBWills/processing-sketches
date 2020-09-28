package coordinate

import util.cos
import util.sin
import kotlin.properties.Delegates.notNull

/**
 * Represents degrees from 0 to 360.
 *
 * On a 2d grid, assume 0 equals pointing right.
 */
class Deg(val value: Int) {
  companion object {
    val HORIZONTAL = Deg(0)
    val UP_45 = Deg(45)
    val DOWN_45 = Deg(135)
    val VERTICAL = Deg(90)
  }

  init {
    if (value !in 0..360) {
      throw Exception("Degree value outside of [0,360] provided: $value")
    }
  }

  val rad: Float
    get() = Math.toRadians(value.toDouble()).toFloat()

  val unitVector: Point
    get() = Point(rad.sin(), rad.cos())

  operator fun plus(other: Deg) = Deg(value + other.value)

  operator fun minus(other: Deg) = Deg(value - other.value)

  fun isHorizontal() = value == 0 || value == 180 || value == 360
  fun isVertical() = value == 90 || value == 270
  override fun toString(): String {
    return "Deg(value=$value)"
  }


}