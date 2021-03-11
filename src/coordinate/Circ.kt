package coordinate

import coordinate.BoundRect.Companion.centeredRect
import geomerativefork.src.RShape
import interfaces.shape.Walkable
import util.atAmountAlong
import util.circleintersection.LCircle
import util.circleintersection.LVector2
import util.circleintersection.getIntersectionPoints
import util.equalsDelta
import util.lessThanEqualToDelta
import util.notEqualsZero
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun Point.isOnCircle(c: Circ) = c.isOnCircle(this)
fun Point.isInCircle(c: Circ) = c.isInCircle(this)

fun Point.toLVector() = LVector2(x.toDouble(), y.toDouble())

open class Circ(val origin: Point, val radius: Double) : Walkable {
  constructor(origin: Point, radius: Number) : this(origin, radius.toDouble())
  constructor(radius: Number) : this(Point.Zero, radius.toDouble())
  constructor(c: Circ) : this(c.origin, c.radius)

  val circumference: Double get() = 2 * PI * radius
  val bounds: BoundRect get() = centeredRect(origin, diameter, diameter)

  init {
    if (radius < 0) {
      throw Exception("Tried to make a circle with a negative radius: $radius and origin: $origin")
    }
  }

  fun toLCircle(): LCircle = LCircle(origin.toLVector(), radius)

  fun moved(amt: Point) = Circ(origin + amt, radius)

  fun angleAtPoint(p: Point): Deg = (p - origin).angle()

  fun pointAtAngle(d: Deg): Point = pointAtRad(d.rad)
  fun pointAtRad(rad: Double): Point = (radius * Point(cos(rad), sin(rad))) + origin

  fun isOnCircle(p: Point) = radius.notEqualsZero() && origin.dist(p).equalsDelta(radius)
  fun isInCircle(p: Point) = radius.notEqualsZero() && origin.dist(p).lessThanEqualToDelta(radius)

  fun contains(p: Point) = origin.dist(p) <= radius

  fun toRShape() = RShape.createCircle(origin.toRPoint(), diameter)

  fun bound(s: Segment): Segment? {
    if (contains(s.p1) && contains(s.p2)) return s
    val intersections = getIntersectionPoints(s)

    if (intersections.size < 2) return null

    return Segment(intersections[0], intersections[1]).withReorientedDirection(s)
  }

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> {
    val startRad = 0.0
    val endRad = 2 * PI
    val numSteps = (circumference / step).toInt()

    return (0 until numSteps).map { i ->
      val radians = (startRad..endRad).atAmountAlong(i / numSteps.toDouble())

      block(pointAtRad(radians))
    }
  }

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
