package coordinate

import util.toDegrees
import java.awt.geom.Line2D
import kotlin.math.abs
import kotlin.math.atan

class LineSegment(
  var p1: Point,
  var p2: Point,
) {

  constructor(p1: Point, len: Number, slope: Deg)
    : this(p1, p1 + (len * slope.unitVector))

  val length: Float
    get() = p1.dist(p2)

  val slope: Deg
    get() {
      if (p2.x - p1.x == 0f) return Deg(90)
      return Deg(atan((p2.y - p1.y) / (p2.x - p1.x)).toDegrees())
    }

  fun flip() = LineSegment(p2, p1)

  fun toLine2d(): Line2D = Line2D.Float(p1.x, p1.y, p2.x, p2.y)

  fun toLine() = Line(p1, slope)

  fun contains(c: Point): Boolean {
    val crossProduct = (c.y - p1.y) * (p2.x - p1.x) - (c.x - p1.x) * (p2.y - p1.y)

    // compare versus epsilon for floating point values, or != 0 if using integers
    if (abs(crossProduct) != 0f) {
      return false
    }

    val dotProduct = (c.x - p1.x) * (p2.x - p1.x) + (c.y - p1.y)*(p2.y - p1.y)
    if (dotProduct < 0) {
      return false
    }

    val squaredLength = (p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y)
    if (dotProduct > squaredLength) {
      return false
    }

    return true
  }

  fun getOverlapWith(other: LineSegment): LineSegment? {
    val shorter = if (other.length >= length) this else other
    val longer = if (other.length < length) this else other

    var segment: LineSegment? = null
    if (!longer.contains(shorter.p1) && !longer.contains(shorter.p2)) {
      return null
    } else if (longer.contains(shorter.p1) && longer.contains(shorter.p2)) {
      segment = shorter
    } else if (longer.contains(shorter.p1)) {
      segment = if (shorter.contains(longer.p1)) {
        LineSegment(longer.p1, shorter.p1)
      } else {
        LineSegment(longer.p2, shorter.p1)
      }
    } else if (longer.contains(shorter.p2)) {
      segment = if (shorter.contains(longer.p1)) {
        LineSegment(longer.p1, shorter.p2)
      } else {
        LineSegment(longer.p2, shorter.p2)
      }
    }

    return if (segment?.length != null && segment.length > 0) segment else null
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LineSegment

    if (p1 != other.p1) return false
    if (p2 != other.p2) return false

    return true
  }

  override fun hashCode(): Int {
    var result = p1.hashCode()
    result = 31 * result + p2.hashCode()
    return result
  }

  override fun toString(): String {
    return "LineSegment(p1=$p1, p2=$p2, slope=$slope)"
  }
}