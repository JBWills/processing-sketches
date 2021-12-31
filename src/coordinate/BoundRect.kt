package coordinate

import appletExtensions.PAppletExt
import appletExtensions.draw.rect
import coordinate.transforms.ShapeTransform
import de.lighti.clipper.Clipper.ClipType
import interfaces.shape.Maskable
import interfaces.shape.Transformable
import interfaces.shape.Walkable
import kotlinx.serialization.Serializable
import util.atAmountAlong
import util.iterators.mapWithNextCyclical
import util.numbers.equalsZero
import util.numbers.min
import util.polylines.PolyLine
import util.polylines.clipping.clip
import util.polylines.rotate
import util.polylines.transform
import kotlin.math.abs
import kotlin.math.max

@Serializable
data class BoundRect(
  val topLeft: Point,
  val width: Double,
  val height: Double,
) : Walkable, Maskable, Transformable<BoundRect> {
  val size: Point
    get() = Point(width, height)

  constructor(topLeft: Point, width: Number, height: Number) : this(
    topLeft,
    width.toDouble(),
    height.toDouble(),
  )

  constructor(topLeft: Point, bottomRight: Point) : this(
    topLeft,
    if (bottomRight == topLeft) 0 else (bottomRight - topLeft).x,
    if (bottomRight == topLeft) 0 else (bottomRight - topLeft).y,
  )

  constructor(width: Number, height: Number) : this(Point.Zero, width, height)

  init {
    if (height < 0) {
      throw Exception("Can't make a rect with negative height: $height")
    } else if (width < 0) {
      throw Exception("Can't make a rect with negative width: $width")
    }
  }

  val top: Double get() = topLeft.y
  val left: Double get() = topLeft.x
  val bottom: Double get() = topLeft.y + height
  val right: Double get() = topLeft.x + width
  val bottomRight get() = Point(right, bottom)
  val topRight get() = Point(right, top)
  val bottomLeft get() = Point(left, bottom)
  val xRange get() = left..right
  val yRange get() = top..bottom

  val area get() = width * height

  val minMax: Pair<Point, Point> get() = topLeft to bottomRight

  val leftPx: Int get() = left.toInt()
  val topPx: Int get() = top.toInt()

  val widthPx: Int get() = width.toInt()
  val heightPx: Int get() = height.toInt()

  val xPixels: IntRange get() = left.toInt()..right.toInt()
  val yPixels: IntRange get() = top.toInt()..bottom.toInt()

  val topSegment get() = Segment(topLeft, topRight)
  val bottomSegment get() = Segment(bottomLeft, bottomRight)
  val leftSegment get() = Segment(topLeft, bottomLeft)
  val rightSegment get() = Segment(topRight, bottomRight)

  val points: List<Point> get() = listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft)

  val center get() = Point(left + width / 2, top + height / 2)

  val pointsClockwise get() = listOf(topLeft, topRight, bottomRight, bottomLeft)

  val segments get() = setOf(topSegment, bottomSegment, leftSegment, rightSegment)

  fun unionBound(other: BoundRect) = BoundRect(
    Point.minXY(this.topLeft, other.topLeft),
    Point.maxXY(this.bottomRight, other.bottomRight),
  )

  fun isTop(line: Line) = line.origin.y == top && line.slope.isHorizontal()
  fun isBottom(line: Line) = line.origin.y == bottom && line.slope.isHorizontal()
  fun isLeft(line: Line) = line.origin.x == left && line.slope.isVertical()
  fun isRight(line: Line) = line.origin.x == right && line.slope.isVertical()

  fun toPolyLine() = listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft)

  /**
   * "Simple" polygons don't have overlapping points. So this may look like an open line if you
   * drew it directly, but utils like Clipper might work better with it if it's simplified.
   */
  fun toSimplePolyLine() = listOf(topLeft, topRight, bottomRight, bottomLeft)

  fun boundsIntersection(other: BoundRect): BoundRect? {
    if (right <= other.left || other.right <= left) return null
    if (bottom <= other.top || other.bottom <= top) return null

    val maxLeft = max(left, other.left)
    val maxTop = max(top, other.top)
    val minRight = min(right, other.right)
    val minBottom = min(bottom, other.bottom)

    return BoundRect(Point(maxLeft, maxTop), Point(minRight, minBottom))
  }

  fun expand(amountX: Number, amountY: Number) = BoundRect(
    topLeft - Point(amountX, amountY),
    width + 2 * amountX.toDouble(),
    height + 2 * amountY.toDouble(),
  )

  fun expand(amount: Number) = expand(amount, amount)
  fun expand(amount: Point) = expand(amount.x, amount.y)

  /**
   * Move the top left corner without changing the bottom right corner.
   *
   * If this intersects the bottom right corner, the new top right will
   * instead be the bottom left of the bounds.
   *
   * @param delta the amount to move the topLeft coord
   */
  fun moveTopLeft(delta: Point) = createBoundRectSafe(topLeft + delta, bottomRight)

  /**
   * Move the top left corner without changing the bottom right corner.
   *
   * If this intersects the bottom right corner, the new top right will
   * instead be the bottom left of the bounds.
   *
   * @param delta the amount to move the topLeft coord
   */
  fun moveBottomRight(delta: Point) = createBoundRectSafe(topLeft, bottomRight + delta)

  fun expandToInclude(p: Point): BoundRect {
    if (contains(p)) return this.copy()
    return BoundRect(Point.minXY(p, topLeft), Point.maxXY(p, bottomRight))
  }

  fun shrink(amountX: Number, amountY: Number) = expand(-amountX.toDouble(), -amountY.toDouble())
  fun shrink(amount: Number) = expand(-amount.toDouble())
  fun shrink(amount: Point) = expand(-amount)

  fun scaled(scaleFactor: Double) = scaled(Point(scaleFactor), center)
  fun scaled(scaleFactor: Point) = scaled(scaleFactor, center)

  override fun scaled(scale: Point, anchor: Point) =
    BoundRect(topLeft.scaled(scale, anchor), width * scale.x, height * scale.y)

  fun scaleByHeight(newHeight: Number): BoundRect {
    val ratio = newHeight.toDouble() / height
    val newWidth = ratio * width
    return BoundRect(topLeft, newWidth, newHeight)
  }

  fun scaleByWidth(newWidth: Number): BoundRect {
    val ratio = newWidth.toDouble() / width
    val newHeight = ratio * height
    return BoundRect(topLeft, newWidth, newHeight)
  }

  override fun rotated(deg: Deg, anchor: Point): PolyLine = points.rotate(deg, anchor)

  override fun translated(translate: Point) = plus(translate)

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

  fun mapped(transformGroup: ShapeTransform) = points.transform(transformGroup)
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

  private fun roughDistFromSides(point: Point): Double = min(
    abs(point.x - left),
    abs(point.x - right),
    abs(point.y - top),
    abs(point.y - bottom),
  )

  override fun intersection(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    polyLine.clip(toPolyLine(), ClipType.INTERSECTION)

  override fun diff(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    toPolyLine().clip(polyLine, ClipType.DIFFERENCE)

  fun getBoundSegment(line: Segment): Segment? {
    if (contains(line.p1) && roughDistFromSides(line.p1) > line.length + 5) return Segment(line)

    return getBoundSegment(line.toLine())
      ?.getOverlapWith(line)
      ?.withReorientedDirection(line)
  }

  override fun intersection(line: Line, memoized: Boolean): List<Segment> =
    getBoundSegment(line)?.let {
      listOf(it)
    } ?: listOf()

  override fun intersection(segment: Segment, memoized: Boolean) = getBoundSegment(segment)?.let {
    listOf(it)
  } ?: listOf()

  override fun diff(segment: Segment, memoized: Boolean): List<Segment> =
    getBoundSegment(segment)?.let { boundSegment ->
      val s1 = Segment(segment.p1, boundSegment.p1)
      val s2 = Segment(boundSegment.p2, segment.p2)
      listOf(s1, s2).filterNot { it.length.equalsZero() }
    } ?: listOf(segment)

  override fun contains(p: Point) = p.y in top..bottom && p.x in left..right

  @JvmName("containsLine")
  fun contains(line: PolyLine): Boolean = contains(listOf(line))

  @JvmName("containsLines")
  fun contains(lines: List<PolyLine>): Boolean = lines.all { line ->
    val yRange = top..bottom
    val xRange = left..right
    line.all { it.y in yRange && it.x in xRange }
  }

  override fun draw(sketch: PAppletExt) = sketch.rect(this)

  override fun toString(): String {
    return "BoundRect(top=$top, left=$left, bottom=$bottom, right=$right)"
  }

  operator fun minus(p: Point): BoundRect = BoundRect(topLeft - p, width, height)
  operator fun plus(p: Point): BoundRect = BoundRect(topLeft + p, width, height)
  operator fun times(p: Point): BoundRect = BoundRect(topLeft * p, bottomRight * p)
  operator fun div(p: Point): BoundRect = BoundRect(topLeft / p, bottomRight / p)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BoundRect

    if (topLeft != other.topLeft) return false
    if (width != other.width) return false
    if (height != other.height) return false

    return true
  }

  override fun hashCode(): Int {
    var result = topLeft.hashCode()
    result = 31 * result + width.hashCode()
    result = 31 * result + height.hashCode()
    return result
  }


  companion object {
    fun Point.mappedOnto(r: BoundRect) = Point(r.left + (x * r.width), r.top + (y * r.height))

    fun centeredRect(center: Point, width: Number, height: Number) = BoundRect(
      Point(center) - Point(width.toDouble() / 2.0, height.toDouble() / 2.0),
      width,
      height,
    )

    fun centeredRect(center: Point, size: Point) = centeredRect(center, size.x, size.y)

    /**
     * Try to create the rect with the given topleft and bottomRight points, but if it would result
     * in a negative width or height, switch the points so no matter what it will make a valid rect.
     *
     * Note that a rect with width or height 0 is valid, but negative height is not.
     *
     * @param topLeft The top left coordinate of the rectangle assuming coordinates go down as you
     *   move up and to the left.
     * @param bottomRight The bottom right coordinate of the rectangle
     */
    fun createBoundRectSafe(topLeft: Point, bottomRight: Point) =
      BoundRect(Point.minXY(topLeft, bottomRight), Point.maxXY(topLeft, bottomRight))
  }
}
