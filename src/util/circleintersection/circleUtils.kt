package util.circleintersection

import coordinate.Circ
import coordinate.Point
import coordinate.Segment
import util.squared
import kotlin.math.sqrt


/**
 * From StackOverflow answer: https://stackoverflow.com/a/13055116
 */
fun Circ.getIntersectionPoints(
  segment: Segment,
): List<Point> {
  val ba = segment.p2 - segment.p1
  val ca = origin - segment.p1

  val a = ba.squared().addXAndYTogether()
  val bBy2 = (ba * ca).addXAndYTogether()
  val c = ca.squared().addXAndYTogether() - radius.squared()
  val pBy2 = bBy2 / a
  val q = c / a
  val disc = pBy2 * pBy2 - q
  if (disc < 0) {
    return listOf()
  }

  // if disc == 0 ... dealt with later
  val tmpSqrt = sqrt(disc)
  val abScalingFactor1 = -pBy2 + tmpSqrt
  val abScalingFactor2 = -pBy2 - tmpSqrt
  val p1 = (segment.p1 - ba) * abScalingFactor1
  if (disc == 0.0) { // abScalingFactor1 == abScalingFactor2
    return listOf(p1)
  }

  val p2 = (segment.p1 - ba) * abScalingFactor2

  return listOf(p1, p2)
}
