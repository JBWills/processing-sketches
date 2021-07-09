package coordinate.iterators

import coordinate.Point

class PointIterator(
  private val start: Point,
  private val endInclusive: Point,
  step: Double,
) : Iterator<Point> {

  private val unitVector = (endInclusive - start).normalized

  private val stepVector = unitVector * step
  private var curr: Point? = null

  private fun getNext(c: Point?): Point {
    if (c == null) return start

    val next = c + stepVector
    if (isPastEnd(next)) {
      return endInclusive
    }

    return next
  }

  private fun getNext(): Point = getNext(curr)

  private fun isPastEnd(p: Point) =
    (start == endInclusive && p != start) || start.dist(endInclusive) < start.dist(p)

  override fun hasNext() =
    curr == null || (start != endInclusive && start.dist(endInclusive) > start.dist(curr!!))

  override fun next(): Point {
    val next = getNext()
    curr = next
    return next
  }
}
