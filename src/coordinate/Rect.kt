package coordinate

data class PaddingRect(
  val base: Float = 0f,
  val vertical: Float = base,
  val horizontal: Float = base,
  val top: Float = vertical,
  val bottom: Float = vertical,
  val left: Float = horizontal,
  val right: Float = horizontal,
) {
  fun totalHorizontal() = left + right
  fun totalVertical() = top + bottom
}

data class BoundRect(
  val topLeft: Point,
  val height: Float,
  val width: Float,
) {
  val top: Float = topLeft.y
  val left: Float = topLeft.x
  val bottom: Float = topLeft.y + height
  val right: Float = topLeft.x + width
  val bottomRight = Point(right, bottom)
  val topRight = Point(right, top)
  val bottomLeft = Point(left, bottom)

  val topSegment get() = LineSegment(topLeft, topRight)
  val bottomSegment get() = LineSegment(bottomLeft, bottomRight)
  val leftSegment get() = LineSegment(topLeft, bottomLeft)
  val rightSegment get() = LineSegment(topRight, bottomRight)

  fun isTop(line: Line) = line.crossesThrough.y == top && line.slope.isHorizontal()
  fun isBottom(line: Line) = line.crossesThrough.y == bottom && line.slope.isHorizontal()
  fun isLeft(line: Line) = line.crossesThrough.y == left && line.slope.isVertical()
  fun isRight(line: Line) = line.crossesThrough.y == right && line.slope.isVertical()

  fun getBoundSegment(line: Line): LineSegment? {
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
    return LineSegment(intersections[0], intersections[1])
  }

  fun inRect(p: Point) = p.y in top..bottom && p.x in left..right

  override fun toString(): String {
    return "BoundRect(top=$top, left=$left, bottom=$bottom, right=$right)"
  }
}