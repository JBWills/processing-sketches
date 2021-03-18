package coordinate

import geomerativefork.src.RPath
import geomerativefork.src.RPoint
import geomerativefork.src.RShape
import interfaces.shape.Walkable
import util.atAmountAlong
import util.iterators.mapArray
import util.iterators.mapWithNextCyclical
import util.min
import kotlin.math.abs

data class BoundRect(
  val topLeft: Point,
  val height: Double,
  val width: Double,
) : Walkable {
  val size: Point
    get() = Point(width, height)

  constructor(topLeft: Point, height: Number, width: Number) : this(
    topLeft, height.toDouble(),
    width.toDouble()
  )

  constructor(topLeft: Point, size: Point) : this(topLeft, size.x, size.y)

  init {
    if (height < 0) {
      throw Exception("Can't make a rect with negative height: $height")
    } else if (width < 0) {
      throw Exception("Can't make a rect with negative width: $width")
    }
  }

  val top: Double by lazy { topLeft.y }
  val left: Double by lazy { topLeft.x }
  val bottom: Double by lazy { topLeft.y + height }
  val right: Double by lazy { topLeft.x + width }
  val bottomRight by lazy { Point(right, bottom) }
  val topRight by lazy { Point(right, top) }
  val bottomLeft by lazy { Point(left, bottom) }

  val topSegment by lazy { Segment(topLeft, Deg(0), width) }
  val bottomSegment by lazy { Segment(bottomLeft, Deg(0), width) }
  val leftSegment by lazy { Segment(topLeft, bottomLeft) }
  val rightSegment by lazy { Segment(topRight, bottomRight) }

  val points: List<Point> by lazy { listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft) }
  val rPoints: Array<RPoint> by lazy { points.mapArray { it.toRPoint() } }

  val center by lazy { Point(left + width / 2, top + height / 2) }

  val pointsClockwise by lazy { listOf(topLeft, topRight, bottomRight, bottomLeft) }

  val segments by lazy { listOf(topSegment, bottomSegment, leftSegment, rightSegment) }

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

  fun atXAmount(percent: Double) = (left..right).atAmountAlong(percent)
  fun atYAmount(percent: Double) = (top..bottom).atAmountAlong(percent)
  fun pointAt(percentX: Double, percentY: Double) = Point(atXAmount(percentX), atYAmount(percentY))

  fun getBoundSegment(line: Line): Segment? {
    if (isTop(line)) return topSegment
    if (isBottom(line)) return bottomSegment
    if (isLeft(line)) return leftSegment
    if (isRight(line)) return rightSegment

    val intersections = listOfNotNull(
      line.intersection(topSegment),
      line.intersection(bottomSegment),
      line.intersection(leftSegment),
      line.intersection(rightSegment)
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
    abs(point.x - left),
    abs(point.x - right),
    abs(point.y - top),
    abs(point.y - bottom)
  )

  fun toRShape(): RShape = RShape.createRectangle(center.toRPoint(), w = width, h = height)
  fun toRPath(): RPath = RPath(rPoints).also { it.addClose() }

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

    fun centeredRect(center: Point, height: Number, width: Number) =
      BoundRect(
        Point(center) - Point(width.toDouble() / 2.0, height.toDouble() / 2.0),
        height,
        width
      )

    fun centeredRect(center: Point, size: Point) = centeredRect(center, size.x, size.y)
  }
}
