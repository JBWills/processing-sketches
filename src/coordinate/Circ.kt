package coordinate

import util.circleintersection.LCircle
import util.circleintersection.LVector2
import kotlin.math.cos
import kotlin.math.sin

fun Point.isOnCircle(c: Circ) = c.isOnCircle(this)
fun Point.isInCircle(c: Circ) = c.isInCircle(this)

fun Point.toLVector() = LVector2(x.toDouble(), y.toDouble())

open class Circ(var origin: Point, var radius: Float) {
  constructor(origin: Point, radius: Number) : this(origin, radius.toFloat())
  constructor(radius: Number) : this(Point.Zero, radius.toFloat())
  constructor(c: Circ) : this(c.origin, c.radius)

  init {
    if (radius < 0) {
      throw Exception("Tried to make a circle with a negative radius: $radius and origin: $origin")
    }
  }

  fun toLCircle(): LCircle = LCircle(origin.toLVector(), radius.toDouble())

  fun angleAtPoint(p: Point): Deg = (p - origin).angle()

  fun pointAtAngle(d: Deg): Point = (radius * Point(cos(d.rad), sin(d.rad))) + origin

  fun isOnCircle(p: Point) = radius != 0f && origin.dist(p) == radius
  fun isInCircle(p: Point) = radius != 0f && origin.dist(p) <= radius

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Circ

    if (origin != other.origin) return false
    if (radius != other.radius) return false

    return true
  }

  override fun hashCode(): Int {
    var result = origin.hashCode()
    result = 31 * result + radius.hashCode()
    return result
  }

  override fun toString(): String {
    return "Circ(origin=$origin, radius=$radius)"
  }

  val diameter get() = 2 * radius
}
