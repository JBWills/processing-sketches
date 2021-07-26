package coordinate.iterators

import coordinate.Point
import coordinate.Segment
import coordinate.Segment3
import interfaces.shape.Scalar

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

class ScalarProgression<T : Scalar<T>>(
  override val start: T,
  override val endInclusive: T,
  private val step: Double = 1.0,
) : Iterable<T>, ClosedRange<T> {

  val segment: Segment3 get() = Segment3(start, endInclusive)

  constructor(s: Segment3, step: Double = 1.0) : this(s.p1, s.p2, step)

  override fun iterator(): Iterator<T> =
    if (step > 0) ScalarIterator(start, endInclusive, step)
    else ScalarIterator(endInclusive, start, -step)

  infix fun step(moveAmount: Double) =
    ScalarProgression(start, endInclusive, moveAmount)

  fun expand(amt: Number) = ScalarProgression(segment.expand(amt), step)
}
