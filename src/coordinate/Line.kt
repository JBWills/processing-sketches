package coordinate

import kotlinx.serialization.Serializable
import util.equalsZero
import java.awt.geom.Line2D

@Serializable
open class Line(
  val origin: Point,
  val slope: Deg,
) {
  fun getPointAtDist(dist: Double) = origin + (slope.unitVector * dist)

  fun normal(clockWise: Boolean = true): Line {
    val rotation = if (clockWise) -90 else 90
    return Line(origin, slope + rotation)
  }

  fun angleBetween(other: Line): Double = slope.rotation(other.slope)

  fun intersection(other: Line): Point? {
    val this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  open fun intersection(other: Segment): Point? {
    val this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  fun intersectsY(y: Double): Point? = intersection(Line(Point(0, y), Deg(0)))
  fun intersectsX(x: Double): Point? = intersection(Line(Point(x, 0), Deg(90)))

  private fun toLine2d(): Line2D {
    val extender = slope.unitVector * 1_000_000
    val startPoint = origin - extender
    val endPoint = origin + extender
    return Line2D.Double(startPoint.x, startPoint.y, endPoint.x, endPoint.y)
  }

  fun getIntersectPoint(l1: Line2D, l2: Line2D): Point? {
    val denominator = (
      (l1.p2.x - l1.p1.x) * (l2.p2.y - l2.p1.y) -
        (l1.p2.y - l1.p1.y) * (l2.p2.x - l2.p1.x))

    if (denominator.equalsZero()) return null

    val numerator = (
      (l1.p1.y - l2.p1.y) * (l2.p2.x - l2.p1.x) -
        (l1.p1.x - l2.p1.x) * (l2.p2.y - l2.p1.y))
    val r = numerator / denominator

    val intersectX = l1.p1.x + r * (l1.p2.x - l1.p1.x)
    val intersectY = l1.p1.y + r * (l1.p2.y - l1.p1.y)

    return Point(intersectX, intersectY)
  }

  override fun toString(): String {
    return "Line(crossesThrough=$origin, slope=$slope)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Line

    if (origin != other.origin) return false
    if (slope != other.slope) return false

    return true
  }

  override fun hashCode(): Int {
    var result = origin.hashCode()
    result = 31 * result + slope.hashCode()
    return result
  }
}
