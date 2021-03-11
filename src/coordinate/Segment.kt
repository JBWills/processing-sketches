package coordinate

import geomerativefork.src.RShape
import interfaces.shape.Walkable
import util.equalsZero
import util.greaterThanEqualToDelta
import util.notEqualsZero
import util.toDegrees
import java.awt.geom.Line2D
import kotlin.math.abs
import kotlin.math.atan

fun getSlope(p1: Point, p2: Point): Deg {
  val denominator = p2.x - p1.x
  var deg = if (denominator.equalsZero()) {
    Deg(90)
  } else {
    Deg(atan((p1.y - p2.y) / denominator).toDegrees())
  }

  // Check that the slope is pointing the right direction
  if (p1 + deg.unitVector * p1.dist(p2) != p2) {
    deg = Deg(deg.value + 180)
  }

  return deg
}

class Segment(
  p1: Point,
  slope: Deg,
  var length: Double,
) : Line(p1, slope), Walkable {
  constructor(p1: Point, p2: Point) : this(p1, getSlope(p1, p2), p1.dist(p2))
  constructor(s: Segment) : this(s.p1, s.p2)
  constructor(p1: Point, len: Number, slope: Deg)
    : this(p1, slope, len.toDouble())

  val center = p1 + slope.unitVector * (length / 2)
  val p1: Point get() = origin
  val p2: Point get() = p1 + slope.unitVector * length

  fun getPointAtPercent(percent: Double) = origin + (slope.unitVector * (length * percent))

  fun toRShape() = RShape.createLine(p1.xf, p1.yf, p2.xf, p2.yf)

  val points get() = arrayOf(p1, p2)

  val unitVector get() = slope.unitVector

  /**
   * Sometimes segments can get flipped, this flips them back to their correct direction.
   *
   * Must be called with a parallel slope.
   */
  fun withReorientedDirection(d: Deg): Segment {
    // error rate is high when the lengths are very small.
    if (length < 0.3) return Segment(this)

    if (!slope.isParallelWith(d)) {
      throw Exception(
        "Error, trying to reorient Segment with non-parallel direction!\nLine: $this\nNewdir: $d"
      )
    }

    return if (slope.equalsRelaxed(d)) Segment(this) else flip()
  }

  fun expand(amt: Number) =
    Segment(p1 - (slope.unitVector * (amt.toDouble() / 2)), slope, length + amt.toDouble())

  fun withReorientedDirection(l: Segment) = withReorientedDirection(l.slope)

  fun flip() = Segment(p2, p1)

  fun toLine2d(): Line2D = Line2D.Double(p1.x, p1.y, p2.x, p2.y)

  fun toLine() = Line(p1, slope)

  fun toProgression(step: Double = 1.0) = PointProgression(this, step)

  override fun walk(step: Double) = (p1..p2 step step).toList()

  override fun <T> walk(step: Double, block: (Point) -> T) = (p1..p2 step step).map(block)

  fun contains(c: Point): Boolean {
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

  override fun intersect(other: Segment): Point? {
    val this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  fun getOverlapWith(other: Segment): Segment? {
    if (!slope.isParallelWith(other)) {
      throw Exception("Trying to get overlap with non parallel line. \n $this\n $other")
    }

    val otherReoriented = other.withReorientedDirection(this)
    val shorter =
      if (otherReoriented.length.greaterThanEqualToDelta(length)) this else otherReoriented
    val longer = if (otherReoriented.length < length) this else otherReoriented

    var segment: Segment? = null
    if (!longer.contains(shorter.p1) && !longer.contains(shorter.p2)) {
      return null
    } else if (longer.contains(shorter.p1) && longer.contains(shorter.p2)) {
      segment = shorter
    } else if (longer.contains(shorter.p1)) {
      segment = if (shorter.contains(longer.p1)) {
        Segment(longer.p1, shorter.p1)
      } else {
        Segment(longer.p2, shorter.p1)
      }
    } else if (longer.contains(shorter.p2)) {
      segment = if (shorter.contains(longer.p1)) {
        Segment(longer.p1, shorter.p2)
      } else {
        Segment(longer.p2, shorter.p2)
      }
    }

    return if (segment?.length != null && segment.length > 0) segment else null
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Segment

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
