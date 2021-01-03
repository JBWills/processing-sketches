package coordinate

import util.mapWithNextCyclical
import kotlin.math.abs
import kotlin.math.min

data class BoundRect(
  val topLeft: Point,
  val height: Double,
  val width: Double,
) : Walkable {
  constructor(topLeft: Point, height: Number, width: Number) : this(topLeft, height.toDouble(), width.toDouble())

  fun centeredRect(center: Point, height: Number, width: Number) =
    BoundRect(center - Point(width.toDouble() / 2.0, height.toDouble() / 2.0), height, width)

  init {
    if (height < 0) {
      throw Exception("Can't make a rect with negative height: $height")
    } else if (width < 0) {
      throw Exception("Can't make a rect with negative width: $width")
    }
  }

  val top: Double = topLeft.y
  val left: Double = topLeft.x
  val bottom: Double = topLeft.y + height
  val right: Double = topLeft.x + width
  val bottomRight = Point(right, bottom)
  val topRight = Point(right, top)
  val bottomLeft = Point(left, bottom)

  val topSegment get() = Segment(topLeft, Deg(0), width)
  val bottomSegment get() = Segment(bottomLeft, Deg(0), width)
  val leftSegment get() = Segment(topLeft, bottomLeft)
  val rightSegment get() = Segment(topRight, bottomRight)

  val center = Point(left + width / 2, top + height / 2)

  val pointsClockwise get() = listOf(topLeft, topRight, bottomRight, bottomLeft)

  val segments get() = listOf(topSegment, bottomSegment, leftSegment, rightSegment)

  fun isTop(line: Line) = line.origin.y == top && line.slope.isHorizontal()
  fun isBottom(line: Line) = line.origin.y == bottom && line.slope.isHorizontal()
  fun isLeft(line: Line) = line.origin.x == left && line.slope.isVertical()
  fun isRight(line: Line) = line.origin.x == right && line.slope.isVertical()

  fun expand(amount: Double) =
    BoundRect(topLeft - amount, height + 2 * amount, width + 2 * amount)

  fun recentered(newCenter: Point) =
    BoundRect(topLeft - (newCenter - center), height, width)

  fun scaleX(scaleFactor: Double) = centeredRect(center, height, width * scaleFactor)

  fun scaleY(scaleFactor: Double) = centeredRect(center, height * scaleFactor, width)

  fun scale(scaleFactor: Point, newCenter: Point = center) =
    centeredRect(newCenter, height * scaleFactor.y, width * scaleFactor.x)

  fun getBoundSegment(line: Line): Segment? {
    if (isTop(line)) return topSegment
    if (isBottom(line)) return bottomSegment
    if (isLeft(line)) return leftSegment
    if (isRight(line)) return rightSegment

    val intersections = listOfNotNull(
      line.intersect(topSegment),
      line.intersect(bottomSegment),
      line.intersect(leftSegment),
      line.intersect(rightSegment)
    )
      .sortedBy { (x, y) -> y * 1000 + x }

    if (intersections.size != 2 || intersections[0] == intersections[1]) return null
    return Segment(intersections[0], intersections[1])
  }

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> =
    pointsClockwise
      .mapWithNextCyclical { curr, next ->
        Segment(curr, next)
          .walk(step, block)
      }
      .flatten()

  fun roughDistFromSides(point: Point): Double = min(
    min(
      abs(point.x - left),
      abs(point.x - right)
    ),
    min(
      abs(point.y - top),
      abs(point.y - bottom)
    ),
  )

  fun getBoundSegment(line: Segment): Segment? {
    if (inRect(line.p1) && roughDistFromSides(line.p1) > line.length + 5) return Segment(line)

    return getBoundSegment(line.toLine())
      ?.getOverlapWith(line)
      ?.withReorientedDirection(line)
  }

  fun inRect(p: Point) = p.y in top..bottom && p.x in left..right
  fun contains(p: Point) = inRect(p)

  override fun toString(): String {
    return "BoundRect(top=$top, left=$left, bottom=$bottom, right=$right)"
  }

  companion object {
    fun Point.mappedOnto(r: BoundRect) = Point(r.left + (x * r.width), r.top + (y * r.height))
  }
}