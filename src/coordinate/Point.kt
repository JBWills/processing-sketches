package coordinate

import util.squared
import kotlin.math.sqrt

class PointIterator(
  private val start: Point,
  private val endInclusive: Point,
  step: Float,
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
  private val step: Float = 1f,
) : Iterable<Point>, ClosedRange<Point> {

  override fun iterator(): Iterator<Point> =
    PointIterator(start, endInclusive, step)

  infix fun step(moveAmount: Float) = PointProgression(start, endInclusive, moveAmount)

}

data class Point(var x: Float, var y: Float) : Comparable<Point> {
  val magnitude
    get() = sqrt(x.squared() + y.squared())

  val normalized: Point
    get() {
      if (magnitude == 0f) return Point(1f, 0f)
      return Point(x / magnitude, y / magnitude)
    }

  fun dist(other: Point) = (this - other).magnitude

  operator fun unaryMinus() = Point(-x, -y)

  operator fun unaryPlus() = Point(+x, +y)

  operator fun plus(other: Point) = Point(x + other.x, y + other.y)

  operator fun minus(other: Point) = Point(x - other.x, y - other.y)
  operator fun times(other: Number) = Point(x * other.toFloat(), y * other.toFloat())

  override fun compareTo(other: Point) = if (this.magnitude > other.magnitude) 1 else -1

  operator fun rangeTo(other: Point) = PointProgression(this, other)

  fun lerp(to: Point, steps: Int) = this..to step (dist(to) / steps)

  companion object {
    fun add(p1: Point, p2: Point) = p1 + p2
    fun subtract(p1: Point, p2: Point) = p1 - p2
    fun multiply(p1: Point, f: Float) = p1 * f
  }

  override fun toString(): String {
    return "Point(x=$x, y=$y)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Point

    if (x != other.x) return false
    if (y != other.y) return false

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

  companion object {
    fun add(p1: PixelPoint, p2: PixelPoint) = p1 + p2

    fun subtract(p1: PixelPoint, p2: PixelPoint) = p1 - p2
  }
}