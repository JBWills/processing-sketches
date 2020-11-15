package coordinate

import kotlin.math.abs
import kotlin.math.min

data class PaddingRect(
  val base: Double = 0.0,
  val vertical: Double = base,
  val horizontal: Double = base,
  val top: Double = vertical,
  val bottom: Double = vertical,
  val left: Double = horizontal,
  val right: Double = horizontal,
) {
  fun totalHorizontal() = left + right
  fun totalVertical() = top + bottom
}

data class BoundRect(
  val topLeft: Point,
  val height: Double,
  val width: Double,
) {
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

  val segments get() = listOf(topSegment, bottomSegment, leftSegment, rightSegment)

  fun isTop(line: Line) = line.origin.y == top && line.slope.isHorizontal()
  fun isBottom(line: Line) = line.origin.y == bottom && line.slope.isHorizontal()
  fun isLeft(line: Line) = line.origin.x == left && line.slope.isVertical()
  fun isRight(line: Line) = line.origin.x == right && line.slope.isVertical()

  fun expand(amount: Double): BoundRect {
    return BoundRect(topLeft - amount, width + 2 * amount, height + 2 * amount)
  }

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

  override fun toString(): String {
    return "BoundRect(top=$top, left=$left, bottom=$bottom, right=$right)"
  }
}