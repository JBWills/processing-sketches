@file:Suppress("unused")

package coordinate

import coordinate.iterators.PointProgression
import geomerativefork.src.RPoint
import geomerativefork.src.util.bound
import interfaces.math.Mathable
import interfaces.shape.Transformable
import kotlinx.serialization.Serializable
import org.locationtech.jts.geom.Coordinate
import org.opencv.core.Size
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
operator fun Number.div(p: Point) = Point(
  this.toDouble() / if (p.x == 0.0) 0.001 else p.x,
  this.toDouble() / if (p.y == 0.0) 0.001 else p.y,
)

operator fun Number.minus(p: Point) = Point(this.toDouble() - p.x, this.toDouble() - p.y)

@Serializable
data class Point(val x: Double, val y: Double) :
  Comparable<Point>,
  Mathable<Point>,
  Transformable<Point> {
  init {
    if (x.isNaN() || y.isNaN()) throw Exception("Can't create a point with nan values. x=$x, y=$y")
  }

  constructor(p: Number) : this(p, p)
  constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())
  constructor(p: Point) : this(p.x, p.y)

  fun toPixelPoint() = PixelPoint(x.toInt(), y.toInt())

  val xf get() = x.toFloat()
  val yf get() = y.toFloat()

  val xl get() = x.toLong()
  val yl get() = y.toLong()

  val xi get() = x.toInt()
  val yi get() = y.toInt()

  val magnitude get() = sqrt(magnitudeSquared)

  // useful when you don't want to do expensive sqrt() calcs
  val magnitudeSquared get() = x.squared() + y.squared()

  val normalized: Point
    get() =
      if (magnitudeSquared == 0.0) Point(1, 0)
      else Point(x / magnitude, y / magnitude)


  fun dist(other: Point) = (this - other).magnitude

  fun distSquared(other: Point) = (this - other).magnitudeSquared

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

  fun bound(xRange: DoubleRange, yRange: DoubleRange) = Point(x.bound(xRange), y.bound(yRange))

  fun bound(minPoint: Point, maxPoint: Point) =
    bound(minPoint.x..maxPoint.x, minPoint.y..maxPoint.y)

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

  fun toRPoint(): RPoint = RPoint(xf, yf)
  fun toSize(): Size = Size(x, y)

  override fun scaled(scale: Point, anchor: Point): Point {
    val diffVector = minus(anchor)
    val scaledDiffVector = diffVector * scale
    return anchor + scaledDiffVector
  }

  override fun translated(translate: Point) = plus(translate)

  override fun toString(): String {
    return "Point(x=${x.roundedString(5)}, y=${y.roundedString(5)})"
  }

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

    fun Coordinate.toPoint() = Point(x, y)

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

    fun Iterable<Point>.maxXY() = reduce { p1, p2 -> maxXY(p1, p2) }
    fun Iterable<Point>.minXY() = reduce { p1, p2 -> minXY(p1, p2) }

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
    val MIN_VALUE = Point(Double.MIN_VALUE, Double.MIN_VALUE)
    val MAX_VALUE = Point(Double.MAX_VALUE, Double.MAX_VALUE)
  }
}
