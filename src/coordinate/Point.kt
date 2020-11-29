package coordinate

import util.equalsDelta
import util.roundedString
import util.squared
import util.toDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

operator fun Number.times(p: Point) = p * this
operator fun Number.plus(p: Point) = p + this

class PointIterator(
  private val start: Point,
  private val endInclusive: Point,
  step: Double,
) : Iterator<Point> {

  private var curr = start

  private val unitVector = (endInclusive - start).normalized

  private val stepVector = unitVector * step

  private fun getNext() = curr + stepVector

  override fun hasNext() = start.dist(endInclusive) >= start.dist(getNext())

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
    PointIterator(start, endInclusive, step)

  infix fun step(moveAmount: Double) = PointProgression(start, endInclusive, moveAmount)

  fun expand(amt: Number) = PointProgression(segment.expand(amt), step)
}

data class Point(var x: Double, var y: Double) : Comparable<Point> {
  constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

  fun toPixelPoint() = PixelPoint(x.toInt(), y.toInt())

  val xf get() = x.toFloat()
  val yf get() = y.toFloat()

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

  fun angle(): Deg = Deg(atan2(y, x).toDegrees())

  operator fun unaryMinus() = Point(-x, -y)

  operator fun unaryPlus() = Point(+x, +y)

  fun addX(amt: Number) = Point(x + amt.toDouble(), y)
  fun addY(amt: Number) = Point(x, y + amt.toDouble())

  operator fun plus(other: Point) = Point(x + other.x, y + other.y)
  operator fun plus(other: Number) = Point(x + other.toDouble(), y + other.toDouble())
  operator fun minus(other: Point) = this + -other
  operator fun minus(other: Number) = this + -other.toDouble()
  operator fun div(other: Point) = Point(x / other.x, y / other.y)

  operator fun times(other: Number) = Point(x * other.toDouble(), y * other.toDouble())

  override fun compareTo(other: Point) = if (this.magnitude > other.magnitude) 1 else -1

  operator fun rangeTo(other: Point) = PointProgression(this, other)

  fun lerp(to: Point, steps: Int) = this..to step (dist(to) / steps)

  companion object {
    fun add(p1: Point, p2: Point) = p1 + p2
    fun subtract(p1: Point, p2: Point) = p1 - p2
    fun multiply(p1: Point, f: Double) = p1 * f

    val Zero = Point(0, 0)
    val Up = Point(0, -1)
    val Down = Point(0, 1)
    val Left = Point(-1, 0)
    val Right = Point(1, 0)
    val One = Point(1, 1)
    val Unit = Point(1, 0)
  }

  override fun toString(): String {
    return "Point(x=${x.roundedString()}, y=${y.roundedString()})"
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
}

data class PixelPoint(var x: Int, var y: Int) {
  operator fun unaryMinus() = PixelPoint(-x, -y)

  operator fun unaryPlus() = PixelPoint(+x, +y)

  operator fun plus(other: PixelPoint) = PixelPoint(x + other.x, y + other.y)

  operator fun minus(other: PixelPoint) = PixelPoint(x - other.x, y - other.y)

  fun toPoint() = Point(x, y)

  companion object {
    fun add(p1: PixelPoint, p2: PixelPoint) = p1 + p2

    fun subtract(p1: PixelPoint, p2: PixelPoint) = p1 - p2
  }
}