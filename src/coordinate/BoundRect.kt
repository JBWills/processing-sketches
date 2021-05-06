package coordinate

import appletExtensions.withFillNonNull
import appletExtensions.withStrokeNonNull
import geomerativefork.src.RPath
import geomerativefork.src.RPoint
import geomerativefork.src.RShape
import interfaces.shape.Walkable
import kotlinx.serialization.Serializable
import processing.core.PApplet
import util.atAmountAlong
import util.iterators.mapArray
import util.iterators.mapWithNextCyclical
import util.min
import util.step
import java.awt.Color
import kotlin.math.abs

@Serializable
data class BoundRect(
  val topLeft: Point,
  val width: Double,
  val height: Double,
) : Walkable {
  val size: Point
    get() = Point(width, height)

  constructor(topLeft: Point, width: Number, height: Number) : this(
    topLeft,
    width.toDouble(),
    height.toDouble(),
  )

  constructor(topLeft: Point, bottomRight: Point) : this(
    topLeft,
    (bottomRight - topLeft).x,
    (bottomRight - topLeft).y,
  )

  constructor(width: Number, height: Number) : this(Point.Zero, width, height)

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

  val xPixels: Iterable<Int> by lazy { left.toInt()..right.toInt() }
  val yPixels: Iterable<Int> by lazy { top.toInt()..bottom.toInt() }

  val topSegment by lazy { Segment(topLeft, topRight) }
  val bottomSegment by lazy { Segment(bottomLeft, bottomRight) }
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

  fun asPolyLine() = listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft)

  fun expand(amountX: Number, amountY: Number) = BoundRect(
    topLeft - Point(amountX, amountY),
    width + 2 * amountX.toDouble(),
    height + 2 * amountY.toDouble(),
  )

  fun expand(amount: Number) = expand(amount, amount)
  fun expand(amount: Point) = expand(amount.x, amount.y)

  fun shrink(amountX: Number, amountY: Number) = expand(-amountX.toDouble(), -amountY.toDouble())
  fun shrink(amount: Number) = expand(-amount.toDouble())
  fun shrink(amount: Point) = expand(-amount)

  fun resizeCentered(newSize: Point): BoundRect =
    BoundRect(center - (newSize / 2), newSize.x, newSize.y)

  fun minusPadding(paddingRect: PaddingRect) = BoundRect(
    topLeft + Point(paddingRect.left, paddingRect.top),
    width - paddingRect.totalHorizontal(),
    height - paddingRect.totalVertical(),
  )

  fun minusPaddingHorizontal(paddingRect: PaddingRect) = BoundRect(
    topLeft + Point(paddingRect.left, 0),
    width - paddingRect.totalHorizontal(),
    height,
  )

  fun minusPaddingVertical(paddingRect: PaddingRect) = BoundRect(
    topLeft + Point(0, paddingRect.top),
    width,
    height - paddingRect.totalVertical(),
  )

  fun recentered(newCenter: Point) =
    BoundRect(topLeft - (center - newCenter), width, height)

  fun scaleX(scaleFactor: Double) = centeredRect(center, width * scaleFactor, height)

  fun scaleY(scaleFactor: Double) = centeredRect(center, width, height * scaleFactor)

  fun scale(scaleFactor: Point, newCenter: Point = center) =
    centeredRect(newCenter, width * scaleFactor.x, height * scaleFactor.y)

  fun atXAmount(percent: Double) = (left..right).atAmountAlong(percent)
  fun atYAmount(percent: Double) = (top..bottom).atAmountAlong(percent)
  fun pointAt(percentX: Double, percentY: Double) = Point(atXAmount(percentX), atYAmount(percentY))
  fun pointAt(percent: Point) = Point(atXAmount(percent.x), atYAmount(percent.y))

  fun getBoundSegment(line: Line): Segment? {
    if (isTop(line)) return topSegment
    if (isBottom(line)) return bottomSegment
    if (isLeft(line)) return leftSegment
    if (isRight(line)) return rightSegment

    val intersections = listOfNotNull(
      line.intersection(topSegment),
      line.intersection(bottomSegment),
      line.intersection(leftSegment),
      line.intersection(rightSegment),
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
    abs(point.y - bottom),
  )

  fun toRShape(): RShape = RShape.createRectangle(topLeft.toRPoint(), w = width, h = height)
  fun toRPath(): RPath = RPath(rPoints).also { it.addClose() }

  fun forEachGrid(block: (Point) -> Unit) = forEachSampled(1.0, 1.0, block)

  fun forEachSampled(stepX: Number, stepY: Number, block: (Point) -> Unit) =
    (left..right step stepX.toDouble()).forEach { x ->
      (top..bottom step stepY.toDouble()).forEach { y ->
        block(Point(x, y))
      }
    }

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

    fun centeredRect(center: Point, width: Number, height: Number) =
      BoundRect(
        Point(center) - Point(width.toDouble() / 2.0, height.toDouble() / 2.0),
        width,
        height,
      )

    fun centeredRect(center: Point, size: Point) = centeredRect(center, size.x, size.y)

    fun PApplet.drawRect(boundRect: BoundRect, stroke: Color? = null, fill: Color? = null) =
      withStrokeNonNull(stroke) {
        withFillNonNull(fill) {
          rect(
            boundRect.left.toFloat(),
            boundRect.top.toFloat(),
            boundRect.width.toFloat(),
            boundRect.height.toFloat(),
          )
        }
      }
  }
}
