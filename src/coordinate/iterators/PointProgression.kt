package coordinate.iterators

import coordinate.Point
import coordinate.Segment

class PointProgression(
  override val start: Point,
  override val endInclusive: Point,
  private val step: Double = 1.0,
) : Iterable<Point>, ClosedRange<Point> {

  val segment: Segment get() = Segment(start, endInclusive)

  constructor(s: Segment, step: Double = 1.0) : this(s.p1, s.p2, step)

  override fun iterator(): Iterator<Point> =
    if (step > 0) PointIterator(start, endInclusive, step)
    else PointIterator(endInclusive, start, -step)

  infix fun step(moveAmount: Double) =
    PointProgression(start, endInclusive, moveAmount)

  fun expand(amt: Number) = PointProgression(segment.expand(amt), step)
}
