package util.polylines

import coordinate.Point

fun List<Point>.centroid() =
  if (length == 0.0) Point.Zero
  else reduceRight { p, acc -> p + acc } / size

fun clockwiseComparator(c: Point) = Comparator { a: Point, b: Point ->
  fun ret(boolean: Boolean) = if (boolean) -1 else 1
  if (a.x >= c.x && b.x < c.x) return@Comparator ret(true)
  if (a.x < c.x && b.x >= c.x) return@Comparator ret(false)
  if (a.x == c.x && b.x == c.x) {
    return@Comparator ret(if (a.y >= c.y || b.y >= c.y) a.y > b.y else b.y > a.y)
  }

  // compute the cross product of vectors (c -> a) x (c -> b)

  // compute the cross product of vectors (c -> a) x (c -> b)
  val det: Double =
    (a.x - c.x) * (b.y - c.y) -
      (b.x - c.x) * (a.y - c.y)
  if (det < 0) return@Comparator ret(true)
  if (det > 0) return@Comparator ret(false)

  // points a and b are on the same line from the c
  // check which point is closer to the c

  // points a and b are on the same line from the c
  // check which point is closer to the c
  val d1: Double = (a - c).squared().sum()
  val d2: Double = (b - c).squared().sum()
  return@Comparator ret(d1 > d2)
}

fun List<Point>.sortClockwise(center: Point? = null): List<Point> {
  val centerNonNull = center ?: centroid()

  return sortedWith(clockwiseComparator(centerNonNull))
}
