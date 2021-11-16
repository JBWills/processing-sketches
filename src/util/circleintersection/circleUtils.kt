package util.circleintersection

import coordinate.Circ
import coordinate.Point
import coordinate.Segment
import util.numbers.coerceTo
import util.numbers.lessThan
import util.numbers.squared
import kotlin.math.sqrt

/**
 * From StackOverflow answer: https://stackoverflow.com/a/13055116
 */
fun Circ.getIntersectionPoints(
  segment: Segment,
): List<Point> {
  if (isEmpty()) {
    return if (segment.contains(origin)) listOf(origin.copy()) else listOf()
  } else if (segment.isEmpty()) {
    return if (contains(segment.p1)) listOf(segment.p1) else listOf()
  }

  val ba = segment.p2 - segment.p1
  val ca = origin - segment.p1

  val a = ba.squared().addXAndYTogether()
  val bBy2 = (ba * ca).addXAndYTogether()
  val c = ca.squared().addXAndYTogether() - radius.squared()
  val pBy2 = bBy2 / a
  val q = c / a
  val disc = (pBy2.squared() - q).coerceTo(0)
  val tmpSqrt = sqrt(disc)

  if (disc.lessThan(0)) return listOf()

  // if disc == 0 ... dealt with later
  val abScalingFactor1 = -pBy2 + tmpSqrt
  val p1 = segment.p1 - (ba * abScalingFactor1)

  val abScalingFactor2 = -pBy2 - tmpSqrt
  val p2 = segment.p1 - (ba * abScalingFactor2)

  return if (p1 == p2) listOf(p1) else listOf(p1, p2)
}
