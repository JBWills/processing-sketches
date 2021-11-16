package coordinate.iterators

import coordinate.Point

class PointIterator(
  private val start: Point,
  private val endInclusive: Point,
  step: Double,
  offset: Double = 0.0,
) : Iterator<Point> {
  private val unitVector = (endInclusive - start).normalized

  private val startAfterOffset = unitVector * offset + start

  private val stepVector = unitVector * step
  private var curr: Point? = null

  val dist = startAfterOffset.dist(endInclusive)

  private fun getNext(c: Point?): Point {
    if (c == null) return startAfterOffset

    val next = c + stepVector
    if (isPastEnd(next)) {
      return endInclusive
    }

    return next
  }

  private fun getNext(): Point = getNext(curr)

  private fun isPastEnd(p: Point) =
    (startAfterOffset == endInclusive && p != startAfterOffset) || dist < startAfterOffset.dist(p)

  override fun hasNext() =
    curr == null || (startAfterOffset != endInclusive && dist > startAfterOffset.dist(curr!!))

  override fun next(): Point {
    val next = getNext()
    curr = next
    return next
  }
}
