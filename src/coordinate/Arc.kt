package coordinate

import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.CounterClockwise
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

class Arc(var startDeg: Deg, var lengthClockwise: Float, circle: Circ) : Circ(circle.origin, circle.radius) {
  constructor(circle: Circ) : this(Deg(0), 360f, circle)
  constructor(arc: Arc) : this(arc.startDeg, arc.lengthClockwise, arc)

  constructor(startDeg: Deg, endDeg: Deg, circle: Circ) : this(
    startDeg,
    startDeg.rotation(endDeg, Clockwise),
    circle
  )

  constructor(
    startPoint: Point,
    endPoint: Point,
    circle: Circ,
  ) : this(
    circle.angleAtPoint(startPoint),
    circle.angleAtPoint(endPoint),
    circle
  )

  init {
    if (lengthClockwise > 360) {
      throw Exception("Can't have an arc with angle length  > 360")
    }
  }

  val angleBisector get(): Deg = startDeg + (lengthClockwise / 2)
  val pointAtBisector get(): Point = pointAtAngle(angleBisector)
  val endDeg get(): Deg = startDeg + lengthClockwise

  val endDegUnbound
    get(): Float = startDeg.value + lengthClockwise

  fun rotated(amt: Float) = Arc(startDeg + amt, lengthClockwise, Circ(origin, radius))

  fun flippedVertically() = Arc(-startDeg, -endDeg, Circ(origin, radius))

  val crossesZero get() = endDegUnbound > 360f

  val isSizeZero get() = lengthClockwise == 0f

  fun contains(deg: Number) = contains(Deg(deg.toFloat()))

  fun contains(deg: Deg): Boolean {
    if (isSizeZero) return false
    if (lengthClockwise == 360f) return true

    return when {
      deg.value == startDeg.value || deg.value == endDeg.value -> true
      deg.value > startDeg.value -> deg.value < endDegUnbound
      else -> crossesZero && endDeg.value > deg.value
    }
  }

  fun contains(a: Arc): Boolean {
    if (lengthClockwise == 360f) return true
    if (a.lengthClockwise == 360f) return false

    val distFromStartToStart = startDeg.rotation(a.startDeg, Clockwise)
    val distFromStartToEnd = startDeg.rotation(a.endDeg, Clockwise)
    return distFromStartToStart <= distFromStartToEnd && distFromStartToEnd <= lengthClockwise
  }

  fun getOverlap(other: Arc): List<Arc> {
    if (other.radius != radius ||
      other.origin != origin ||
      lengthClockwise == 0f ||
      other.lengthClockwise == 0f) {
      return listOf()
    }

    if (this.contains(other)) return listOf(other)
    if (other.contains(this)) return listOf(this)

    val (first, second) = listOf(this, other).sortedBy { it.endDeg.value }

    println("first: $first")
    println("second: $second")

    val firstStart = first.startDeg.value
    val firstEnd = first.endDegUnbound

    val secondStart = second.startDeg.value
    val secondEnd = second.endDegUnbound

    return when {
      second.contains(firstStart) && second.contains(firstEnd) -> {
        listOf(
          Arc(second.startDeg, first.endDeg, this),
          Arc(first.startDeg, second.endDeg, this))
          .sortedBy { it.startDeg.value }
          .filterNot { it.lengthClockwise == 0f }
      }
      first.contains(secondStart) -> {
        listOf(Arc(Deg(secondStart), Deg(firstEnd), this)).filterNot { it.lengthClockwise == 0f }
      }
      first.contains(secondEnd) -> {
        listOf(Arc(Deg(firstStart), Deg(secondEnd), this)).filterNot { it.lengthClockwise == 0f }
      }
      else -> listOf()
    }

//    return when {
//      firstStart == secondStart -> listOf(Arc(Deg(firstStart), Deg(min(firstEnd, secondEnd)), this))
//      firstEnd == secondEnd -> listOf(Arc(Deg(max(firstStart, secondStart)), Deg(firstEnd), this))
//
//      firstEnd <= secondStart -> listOf()
//      else -> listOf(Arc(Deg(max(firstStart, secondStart)), Deg(min(firstEnd, secondEnd)), this))
//    }
  }

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