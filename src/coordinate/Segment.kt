package coordinate

import coordinate.iterators.PointProgression
import interfaces.shape.Walkable
import util.base.maybeMap
import util.numbers.equalsZero
import util.numbers.greaterThanEqualToDelta
import util.numbers.notEqualsZero
import util.numbers.toDegrees
import util.polylines.PolyLine
import util.polylines.rotate
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
  val p2: Point
) : Line(p1, getSlope(p1, p2)), Walkable {
  constructor(
    p1: Point,
    slope: Deg,
    length: Double
  ) : this(p1, p1 + (slope.unitVector * length))

  constructor(s: Segment) : this(s.p1, s.p2)
  constructor(p1: Point, len: Number, slope: Deg)
    : this(p1, slope, len.toDouble())

  val length: Double get() = p1.dist(p2)
  val center: Point get() = p1 + slope.unitVector * (length / 2)
  val p1: Point get() = origin

  fun getPointAtPercent(percent: Double) = origin + (slope.unitVector * (length * percent))

  val points: Array<Point> get() = arrayOf(p1, p2)
  val asPolyLine: List<Point> get() = listOf(p1, p2)

  val unitVector get() = slope.unitVector

  val midPoint get() = p1 + unitVector * (length / 2)

  fun splitAtMidpoint() = Pair(Segment(p1, midPoint), Segment(midPoint, p2))

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
        "Error, trying to reorient Segment with non-parallel direction!\nLine: $this\nNewdir: $d",
      )
    }

    return if (slope.equalsRelaxed(d)) Segment(this) else flip()
  }

  fun expand(amt: Number) =
    Segment(p1 - (slope.unitVector * (amt.toDouble() / 2)), slope, length + amt.toDouble())


  fun expandStart(amt: Number) = Segment(p1 - (slope.unitVector * amt.toDouble()), p2)
  fun expandEnd(amt: Number) = Segment(p1, p2 + (slope.unitVector * amt.toDouble()))
  fun resizeCentered(newLength: Number) = expand(newLength.toDouble() - length)
  fun recentered(newCenter: Point) = centered(newCenter, slope, length)

  fun withReorientedDirection(l: Segment) = withReorientedDirection(l.slope)

  fun centeredWithSlope(d: Deg) = centered(center, d, length)

  fun flip() = Segment(p2, p1)

  fun toLine2d(): Line2D = Line2D.Double(p1.x, p1.y, p2.x, p2.y)

  fun toLine() = Line(p1, slope)

  fun toPolyLine(): PolyLine = listOf(p1, p2)

  fun toProgression(step: Double = 1.0) = PointProgression(this, step)

  fun isEmpty() = length == 0.0

  operator fun plus(other: Point) = Segment(p1 + other, p2 + other)
  operator fun minus(other: Point) = Segment(p1 - other, p2 - other)
  operator fun unaryPlus() = Segment(+p1, +p2)
  operator fun unaryMinus() = Segment(-p1, -p2)

  fun combine(other: Segment): Segment {
    if (this.p2 != other.p1) {
      throw Exception("Can't combine two segments when the end of segment 1 doesn't match the start of segment 2.\nSegment 1: $this\nSegment 2: $other")
    }

    return Segment(p1, other.p2)
  }

  override fun scaled(scale: Point, anchor: Point): Segment =
    Segment(p1.scaled(scale, anchor), p2.scaled(scale, anchor))

  override fun translated(translate: Point): Segment =
    Segment(p1.translated(translate), p2.translated(translate))

  override fun rotated(deg: Deg, anchor: Point): PolyLine = points.toList().rotate(deg, anchor)

  override fun walk(step: Double) = (p1..p2 step step).toList()
  fun walk(step: Double, offset: Double) = (p1..p2 step step offset offset).toList()

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

  override fun intersection(other: Segment): Point? {
    val this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  fun getOverlapWith(other: Segment): Segment? {
    if (!slope.isParallelWith(other) && this.length.notEqualsZero() && other.length.notEqualsZero()) {
      throw Exception("Trying to get overlap with non parallel line. \n this: $this\n other: $other")
    }

    val otherReoriented = other.withReorientedDirection(this)
    val (shorter, longer) =
      if (otherReoriented.length.greaterThanEqualToDelta(length)) this to otherReoriented else otherReoriented to this

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

  fun split(numSegments: Number, transform: ((Point) -> Point)? = null): PolyLine {
    val numSegmentsDouble = numSegments.toDouble()

    if (numSegmentsDouble < 1) return points.maybeMap(transform)

    val totalLength = p2.dist(p1)

    return (p1..p2)
      .step(totalLength / numSegmentsDouble)
      .maybeMap(transform)
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
    return "Segment(p1=$p1, p2=$p2, slope=$slope)"
  }

  companion object {
    fun centered(center: Point, slope: Deg, length: Number): Segment =
      Segment(
        center.translated(slope.unitVector * length / 2),
        center.translated(-slope.unitVector * length / 2),
      )

    fun Point.toUnitVectorSegment(center: Point) = Segment(
      center - (normalized / 2),
      center + (normalized / 2),
    )


    fun List<Segment>.move(amount: Point) = map { it + amount }
    fun List<Segment>.segmentsToPolyLines(): List<PolyLine> = map(Segment::asPolyLine)
    fun Pair<Point, Point>.toSegment() = Segment(first, second)
  }
}
