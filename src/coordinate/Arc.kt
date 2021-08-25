package coordinate

import coordinate.RotationDirection.Clockwise
import util.equalsZero
import util.lessThanEqualToDelta
import util.polylines.PolyLine
import util.polylines.rotate
import util.step

class Arc(
  val startDeg: Deg,
  val lengthClockwise: Double,
  circle: Circ,
) : Circ(circle) {
  constructor(startDeg: Deg, lengthClockwise: Number, circle: Circ) : this(
    startDeg,
    lengthClockwise.toDouble(), circle,
  )

  constructor(circle: Circ) : this(Deg(0), 360, circle)
  constructor(arc: Arc) : this(arc.startDeg, arc.lengthClockwise, arc)

  constructor(startDeg: Deg, endDeg: Deg, circle: Circ) : this(
    startDeg,
    startDeg.rotation(endDeg, Clockwise),
    circle,
  )

  constructor(
    startPoint: Point,
    endPoint: Point,
    circle: Circ,
  ) : this(
    circle.angleAtPoint(startPoint),
    circle.angleAtPoint(endPoint),
    circle,
  )

  init {
    if (lengthClockwise > 360.1) {
      throw Exception("Can't have an arc with angle length  > 360")
    }
  }

  val angleBisector: Deg get() = startDeg + (lengthClockwise / 2)
  val pointAtBisector: Point get() = pointAtAngle(angleBisector)
  val endDeg: Deg get() = startDeg + lengthClockwise

  val startPoint get() = pointAtAngle(startDeg)
  val endPoint get() = pointAtAngle(endDeg)

  val arcLength: Double get() = (startDeg.rotation(endDeg, Clockwise) / 360.0) * circumference

  val endDegUnbound: Double get() = startDeg.value + lengthClockwise

  val crossesZero get() = endDegUnbound > 360.0

  val isSizeZero get() = lengthClockwise == 0.0

  fun rotated(amt: Double) = Arc(startDeg + amt, lengthClockwise, Circ(origin, radius))

  fun flippedVertically() = Arc(-startDeg, -endDeg, Circ(origin, radius))

  fun pxToDeg(px: Double) = px * Deg.Whole / circumference
  fun degToPx(deg: Deg) = (circumference / Deg.Whole) * deg.value

  fun expandPixels(numPix: Double): Arc = expandDeg(pxToDeg(numPix))

  fun expandDeg(amt: Double): Arc {
    if (2 * amt + lengthClockwise >= 360) {
      return Arc(startDeg - amt, 360, this)
    } else if ((2 * amt + lengthClockwise).lessThanEqualToDelta(0.0)) {
      return Arc(startDeg + lengthClockwise / 2, 0.0, this)
    }

    return Arc(startDeg - amt, endDeg + amt, this)
  }

  fun contains(deg: Number) = contains(Deg(deg.toDouble()))

  fun contains(deg: Deg): Boolean {
    if (isSizeZero) return false
    if (lengthClockwise == 360.0) return true

    return when {
      deg.value == startDeg.value || deg.value == endDeg.value -> true
      deg.value > startDeg.value -> deg.value < endDegUnbound
      else -> crossesZero && endDeg.value > deg.value
    }
  }

  private fun pointAtLength(length: Double): Point {
    if (circumference == 0.0) return origin
    return pointAtAngle(Deg((length / circumference) * 360.0 + startDeg.value))
  }

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> =
    (0.0..lengthClockwise step step).map { degLengthAlong ->
      block(pointAtAngle(startDeg + degLengthAlong))
    }

  fun contains(a: Arc): Boolean {
    if (lengthClockwise == 360.0) return true
    if (a.lengthClockwise == 360.0) return false

    val distFromStartToStart = startDeg.rotation(a.startDeg, Clockwise)
    val distFromStartToEnd = startDeg.rotation(a.endDeg, Clockwise)
    return distFromStartToEnd in distFromStartToStart..lengthClockwise
  }

  fun getOverlap(other: Arc): List<Arc> {
    if (other.radius != radius ||
      other.origin != origin ||
      lengthClockwise == 0.0 ||
      other.lengthClockwise == 0.0
    ) {
      return listOf()
    }

    if (this.contains(other)) return listOf(other)
    if (other.contains(this)) return listOf(this)

    val (first, second) = listOf(this, other).sortedBy { it.endDeg.value }

    val firstStart = first.startDeg.value
    val firstEnd = first.endDegUnbound

    val secondStart = second.startDeg.value
    val secondEnd = second.endDegUnbound

    return when {
      second.contains(firstStart) && second.contains(firstEnd) -> {
        listOf(
          Arc(second.startDeg, first.endDeg, this),
          Arc(first.startDeg, second.endDeg, this),
        )
          .sortedBy { it.startDeg.value }
          .filterNot { it.lengthClockwise == 0.0 }
      }
      first.contains(secondStart) -> {
        listOf(Arc(Deg(secondStart), Deg(firstEnd), this)).filterNot { it.lengthClockwise == 0.0 }
      }
      first.contains(secondEnd) -> {
        listOf(Arc(Deg(firstStart), Deg(secondEnd), this)).filterNot { it.lengthClockwise == 0.0 }
      }
      else -> listOf()
    }
  }

  fun minusAll(others: List<Arc>): List<Arc> {
    var results = listOf(this)

    others.forEach { other ->
      results = results.map { it - other }.flatten()
    }

    return results
  }

  operator fun minus(other: Arc): List<Arc> = when {
    getOverlap(other).isEmpty() -> listOf(Arc(this))
    // If the other completely contains this, return nothing
    other.contains(this) -> listOf()
    // If this is a full circle, we want to get the inverse
    lengthClockwise == 360.0 -> listOf(Arc(other.endDeg, 360 - other.lengthClockwise, this))
    contains(other) -> listOf(
      Arc(startDeg, other.startDeg, this),
      Arc(other.endDeg, endDeg, this),
    )
    contains(other.startDeg) && !contains(other.endDeg) -> listOf(
      Arc(startDeg, other.startDeg, this),
    )
    contains(other.endDeg) && !contains(other.startDeg) -> listOf(
      Arc(other.endDeg, endDeg, this),
    )
    else -> listOf(
      // if both endpoints are contained but entire arc isn't contained
      Arc(other.endDeg, other.startDeg, this),
    )
  }.filterNot { it.lengthClockwise.equalsZero() }

  override fun scaled(scale: Point, anchor: Point): Arc =
    Arc(startDeg, lengthClockwise, super.scaled(scale, anchor))

  override fun translated(translate: Point): Arc =
    Arc(startDeg, lengthClockwise, super.translated(translate))

  override fun rotated(deg: Deg, anchor: Point): PolyLine = walk(10.0).rotate(deg, anchor)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as Arc

    if (startDeg != other.startDeg) return false
    if (lengthClockwise != other.lengthClockwise) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + startDeg.hashCode()
    result = 31 * result + lengthClockwise.hashCode()
    return result
  }

  override fun toString(): String {
    return "Arc((from, to)=($startDeg, $endDeg), lengthClockwise=$lengthClockwise, c=$origin, r=$radius)"
  }
}
