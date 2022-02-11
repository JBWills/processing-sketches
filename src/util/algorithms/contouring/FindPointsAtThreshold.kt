package util.algorithms.contouring

import coordinate.Point
import coordinate.Segment
import util.polylines.iterators.mapBySegment

private const val MIN_SEGMENT_SIZE = 0.1
private const val WALK_STEP = 1.0

private fun Segment.crossesThreshold(f: (Point) -> Boolean) = f(p1) != f(p2)

/** TODO: remove this. Use MatContourUtil.maskContours instead. */
fun Segment.pointsAtThresholdFine(f: (Point) -> Boolean): List<Point> =
  walk(MIN_SEGMENT_SIZE)
    .mapBySegment { if (it.crossesThreshold(f)) it.midPoint else null }
    .filterNotNull()

/** TODO: remove this. Use MatContourUtil.maskContours instead. */
fun Segment.pointsAtThreshold(f: (Point) -> Boolean): List<Point> =
  walk(WALK_STEP)
    .mapBySegment { if (it.crossesThreshold(f)) it.pointsAtThresholdFine(f) else listOf() }
    .flatten()

private val _pointsAtThresholdMemoizer = { s: Segment, f: (Point) -> Boolean ->
  s.pointsAtThreshold(f)
}

/** TODO: remove this. Use MatContourUtil.maskContours instead. */
fun Segment.pointsAtThresholdMemoized(f: (Point) -> Boolean): List<Point> =
  _pointsAtThresholdMemoizer(this, f)

