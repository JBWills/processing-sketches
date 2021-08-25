package coordinate

import appletExtensions.PAppletExt
import appletExtensions.draw.circle
import coordinate.BoundRect.Companion.centeredRect
import interfaces.shape.Maskable
import interfaces.shape.Transformable
import interfaces.shape.Walkable
import util.atAmountAlong
import util.circleintersection.LCircle
import util.circleintersection.LVector2
import util.circleintersection.getIntersectionPoints
import util.equalsDelta
import util.equalsZero
import util.lessThanEqualToDelta
import util.map
import util.notEqualsZero
import util.polylines.PolyLine
import util.polylines.rotate
import util.squared
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun Point.isOnCircle(c: Circ) = c.isOnCircle(this)
fun Point.isInCircle(c: Circ) = c.isInCircle(this)

fun Point.toLVector() = LVector2(x, y)

open class Circ(val origin: Point, val radius: Double) :
  Walkable,
  Maskable,
  Transformable<Circ> {
  constructor(origin: Point, radius: Number) : this(origin, radius.toDouble())
  constructor(radius: Number) : this(Point.Zero, radius.toDouble())
  constructor(c: Circ) : this(c.origin, c.radius)

  val diameter get() = 2 * radius
  val circumference: Double get() = 2 * PI * radius
  val bounds: BoundRect get() = centeredRect(origin, diameter, diameter)
  val radiusSquared: Double get() = radius.squared()

  init {
    if (radius < 0) {
      throw Exception("Tried to make a circle with a negative radius: $radius and origin: $origin")
    }
  }

  fun toLCircle(): LCircle = LCircle(origin.toLVector(), radius)

  fun angleAtPoint(p: Point): Deg = (p - origin).angle()

  fun pointAtAngle(d: Deg): Point = pointAtRad(d.rad)
  fun pointAtRad(rad: Double): Point = (radius * Point(cos(rad), sin(rad))) + origin

  fun isOnCircle(p: Point) = radius.notEqualsZero() && origin.dist(p).equalsDelta(radius)
  fun isInCircle(p: Point) = radius.notEqualsZero() && origin.dist(p).lessThanEqualToDelta(radius)

  override fun contains(p: Point) = origin.distSquared(p) <= radiusSquared

  override fun intersection(line: Line, memoized: Boolean): List<Segment> {
    TODO("Not yet implemented")
  }

  override fun intersection(segment: Segment, memoized: Boolean): List<Segment> =
    listOfNotNull(bound(segment))

  override fun diff(segment: Segment, memoized: Boolean): List<Segment> =
    bound(segment)?.let { boundSegment ->
      val s1 = Segment(segment.p1, boundSegment.p1)
      val s2 = Segment(boundSegment.p2, segment.p2)
      listOf(s1, s2).filterNot { it.length.equalsZero() }
    } ?: listOf(segment)

  override fun intersection(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    ContinuousMaskedShape(polyLine, this).toBoundPoints(true)

  override fun diff(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    ContinuousMaskedShape(polyLine, this).toBoundPoints(false)

  override fun draw(sketch: PAppletExt) = sketch.circle(this)

  override fun scaled(scale: Point, anchor: Point): Circ =
    Circ(origin.scaled(scale, anchor), radius * scale.x)

  override fun translated(translate: Point): Circ = Circ(origin.translated(translate), radius)

  override fun rotated(deg: Deg, anchor: Point): PolyLine = walk(10.0).rotate(deg, anchor)

  fun toPolyLine(step: Double = 2.0) = walk(step)

  fun bound(s: Segment): Segment? {
    if (contains(s.p1) && contains(s.p2)) return s
    if (origin.perpendicularDistanceTo(s) > radius) return null

    val intersections = getIntersectionPoints(s)

    if (intersections.size < 2) return null

    return Segment(intersections[0], intersections[1])
  }

  fun isEmpty() = radius == 0.0

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> {
    val startRad = 0.0
    val endRad = 2 * PI
    val numSteps = (circumference / step).toInt()

    return numSteps.map { i ->
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
}
