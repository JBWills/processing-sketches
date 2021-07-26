package coordinate

import coordinate.iterators.ScalarProgression
import interfaces.shape.Scalar
import interfaces.shape.Walkable
import java.awt.geom.Line2D
import kotlin.math.abs


class Segment3<T : Scalar<T>>(
  val p1: T,
  val p2: T
) : Walkable {

  val unitVector get() = (p2 - p1).normalized

  val length: Double get() = p1.dist(p2)
  val center: T get() = getPointAtPercent(0.5)

  fun getPointAtPercent(percent: Double) = p1 + (unitVector * (length * percent))

  val points: List<T> get() = listOf(p1, p2)
  val asPolyLine: List<T> get() = listOf(p1, p2)

  fun splitAtMidpoint() = Pair(Segment3(p1, center), Segment3(center, p2))

  fun expand(amt: Number) = (unitVector * (amt.toDouble() / 2)).let { expandVector ->
    Segment3(p1 - expandVector, p2 + expandVector)
  }

  fun resizeCentered(newLength: Number) = expand(newLength.toDouble() - length)
  fun recentered(newCenter: T) = centered(newCenter, slope, length)

  fun withReorientedDirection(l: Segment) = withReorientedDirection(l.slope)

  fun centeredWithSlope(d: Deg) = centered(center, d, length)

  fun flip() = Segment(p2, p1)

  fun toLine2d(): Line2D = Line2D.Double(p1.x, p1.y, p2.x, p2.y)

  fun toLine() = Line(p1, slope)

  fun toProgression(step: Double = 1.0) = ScalarProgression(p1, p2, step)

  fun isEmpty() = length == 0.0

  operator fun plus(other: T) = Segment3(p1 + other, p2 + other)
  operator fun minus(other: T) = Segment3(p1 - other, p2 - other)
  operator fun unaryPlus() = Segment3(+p1, +p2)
  operator fun unaryMinus() = Segment3(-p1, -p2)

  override fun walk(step: Double) = (p1..p2 step step).toList()

  override fun <T> walk(step: Double, block: (Point) -> T) = (p1..p2 step step).map(block)

  operator fun contains(c: Point): Boolean {
    val crossProduct = (c.y - p1.y) * (p2.x - p1.x) - (c.x - p1.x) * (p2.y - p1.y)

    // compare versus epsilon for doubleing point values, or != 0 if using integers
    if (abs(crossProduct).notEqualsZero()) {
      return false
    }

    val dotProduct = (c.x - p1.x) * (p2.x - p1.x) + (c.y - p1.y) * (p2.y - p1.y)
    if (dotProduct < 0) {
      return false
    }

    val squaredLength = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y)
    if (dotProduct > squaredLength) {
      return false
    }

    return true
  }

  override fun toString(): String {
    return "Segment3(p1=$p1, p2=$p2)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Segment3<*>

    if (p1 != other.p1) return false
    if (p2 != other.p2) return false

    return true
  }

  override fun hashCode(): Int {
    var result = p1.hashCode()
    result = 31 * result + p2.hashCode()
    return result
  }

  companion object {
    fun <T : Scalar<T>> centered(center: T, direction: T, length: Double): Segment3<T> =
      Segment3(center, center + direction.normalized).expand(length - 1)

    fun Point.toUnitVectorSegment(center: Point) = Segment(
      center - (normalized / 2),
      center + (normalized / 2),
    )


    fun List<Segment>.move(amount: Point) = map { it + amount }
    fun Pair<Point, Point>.toSegment() = Segment(first, second)
  }
}
