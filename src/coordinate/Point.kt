@file:Suppress("unused")

package coordinate

import geomerativefork.src.RPoint
import geomerativefork.src.util.bound
import interfaces.math.Mathable
import kotlinx.serialization.Serializable
import util.DoubleRange
import util.equalsDelta
import util.equalsZero
import util.roundedString
import util.squared
import util.step
import util.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

operator fun Number.times(p: Point) = p * this
operator fun Number.plus(p: Point) = p + this

class PointIterator(
  private val start: Point,
  private val endInclusive: Point,
  step: Double,
) : Iterator<Point> {

  private val unitVector = (endInclusive - start).normalized

  private val stepVector = unitVector * step
  private var curr: Point? = null

  private fun getNext(c: Point?): Point {
    if (c == null) return start

    val next = c + stepVector
    if (isPastEnd(next)) {
      return endInclusive
    }

    return next
  }

  private fun getNext(): Point = getNext(curr)

  private fun isPastEnd(p: Point) =
    (start == endInclusive && p != start) || start.dist(endInclusive) < start.dist(p)

  override fun hasNext() =
    curr == null || (start != endInclusive && start.dist(endInclusive) > start.dist(curr!!))

  override fun next(): Point {
    val next = getNext()
    curr = next
    return next
  }
}

class PointProgression(
  override val start: Point,
  override val endInclusive: Point,
  private val step: Double = 1.0,
) : Iterable<Point>, ClosedRange<Point> {

  val segment: Segment get() = Segment(start, endInclusive)

  constructor(s: Segment, step: Double = 1.0) : this(s.p1, s.p2, step)

  override fun iterator(): Iterator<Point> =
    if (step > 0) PointIterator(start, endInclusive, step)
    else PointIterator(endInclusive, start, -step)

  infix fun step(moveAmount: Double) = PointProgression(start, endInclusive, moveAmount)

  fun expand(amt: Number) = PointProgression(segment.expand(amt), step)
}

@Serializable
data class Point(val x: Double, val y: Double) : Comparable<Point>, Mathable<Point> {
  init {
    if (x.isNaN() || y.isNaN()) throw Exception("Can't create a point with nan values. x=$x, y=$y")
  }

  constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())
  constructor(p: Point) : this(p.x, p.y)

  fun toPixelPoint() = PixelPoint(x.toInt(), y.toInt())

  val xf = x.toFloat()
  val yf = y.toFloat()

  val xi = x.toInt()
  val yi = y.toInt()

  val magnitude
    get() = sqrt(x.squared() + y.squared())

  val normalized: Point
    get() {
      if (magnitude == 0.0) return Point(1, 0)
      return Point(x / magnitude, y / magnitude)
    }

  fun dist(other: Point) = (this - other).magnitude

  fun flipX() = Point(-x, y)
  fun flipY() = Point(x, -y)

  fun swapXY() = Point(y, x)

  fun angle(): Deg = Deg(atan2(y, x).toDegrees())

  override operator fun unaryMinus() = Point(-x, -y)

  override operator fun unaryPlus() = Point(+x, +y)

  fun squared() = Point(x.squared(), y.squared())

  fun addXAndYTogether() = x + y

  fun boundX(range: DoubleRange) = Point(x.bound(range), y)
  fun boundY(range: DoubleRange) = Point(x, y.bound(range))

  fun bound(xRange: DoubleRange, yRange: DoubleRange) = boundX(xRange).boundY(yRange)

  fun addX(amt: Number) = Point(x + amt.toDouble(), y)
  fun addY(amt: Number) = Point(x, y + amt.toDouble())
  fun withX(amt: Number) = Point(amt, y)
  fun withY(amt: Number) = Point(x, amt)
  fun zeroX() = Point(0, y)
  fun zeroY() = Point(x, 0)

  operator fun plus(other: Point) = Point(x + other.x, y + other.y)
  operator fun plus(other: List<Point>) = listOf(this) + other
  override operator fun plus(other: Number) = Point(x + other.toDouble(), y + other.toDouble())
  operator fun minus(other: Point) = this + -other
  override operator fun minus(other: Number) = this + -other.toDouble()
  operator fun div(other: Point) = Point(x / other.x, y / other.y)
  override operator fun div(other: Number) = this / Point(other, other)

  override operator fun times(other: Number) = this * Point(other, other)
  operator fun times(other: Point) = Point(x * other.x, y * other.y)

  override operator fun compareTo(other: Point): Int =
    if (x != other.x) x.compareTo(other.x)
    else y.compareTo(other.y)

  operator fun rangeTo(other: Point) = PointProgression(this, other)

  fun lineTo(other: Point) = Segment(this, other)

  fun perpendicularDistanceTo(line: Segment): Double {
    val (x0, y0) = this
    val (x1, y1) = line.p1
    val (x2, y2) = line.p2
    val denominator = sqrt((x2 - x1).squared() + (y2 - y1).squared())

    return if (denominator.equalsZero()) dist(line.p1)
    else abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1)) / denominator
  }

  fun lerp(to: Point, steps: Int) = this..to step (dist(to) / steps)

  fun map(block: (Double) -> Number) = Point(block(x), block(y))

  fun forEach2D(block: (Point) -> Unit) = (Zero..this).forEach2D(block)

  override fun toString(): String {
    return "Point(x=${x.roundedString(5)}, y=${y.roundedString(5)})"
  }

  fun toRPoint(): RPoint = RPoint(xf, yf)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Point

    if (!x.equalsDelta(other.x)) return false
    if (!y.equalsDelta(other.y)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = x.hashCode()
    result = 31 * result + y.hashCode()
    return result
  }

  companion object {
    fun add(p1: Point, p2: Point) = p1 + p2
    fun subtract(p1: Point, p2: Point) = p1 - p2
    fun multiply(p1: Point, f: Double) = p1 * f

    operator fun Number.times(p: Point) = p * toDouble()
    operator fun Number.minus(p: Point) = p - toDouble()
    operator fun Number.plus(p: Point) = p + toDouble()
    operator fun Number.div(p: Point) = p / toDouble()

    fun List<Point>.move(amount: Point): List<Point> = map { it + amount }
    fun List<Point>.plusIf(p: Point?): List<Point> = if (p != null) this.plusElement(p) else this
    fun MutableList<Point>.addIf(p: Point?): List<Point> {
      if (p != null) this.add(p)

      return this
    }

    fun zip(p1: Point, p2: Point, block: (Double, Double) -> Number) =
      Point(block(p1.x, p2.x), block(p1.y, p2.y))

    fun Point?.plusIf(other: List<Point>) = if (this != null) this + other else other

    fun minXY(p1: Point, p2: Point) = Point(min(p1.x, p2.x), min(p1.y, p2.y))
    fun maxXY(p1: Point, p2: Point) = Point(max(p1.x, p2.x), max(p1.y, p2.y))

    fun PointProgression.forEach2D(block: (Point) -> Unit) =
      (start.x..endInclusive.x step 1.0).forEach { x ->
        (start.y..endInclusive.y step 1.0).forEach { y ->
          block(Point(x, y))
        }
      }

    val Zero = Point(0, 0)
    val NegativeToPositive = Point(-1, 1)
    val Half = Point(0.5, 0.5)
    val Up = Point(0, -1)
    val Down = Point(0, 1)
    val Left = Point(-1, 0)
    val Right = Point(1, 0)
    val One = Point(1, 1)
    val Unit = Point(1, 0)
  }
}
