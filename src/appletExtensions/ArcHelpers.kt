package appletExtensions

import appletExtensions.IntersectionData.Coincident
import appletExtensions.IntersectionData.Overlapping
import appletExtensions.IntersectionData.SeparateOrExternallyTangent
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import coordinate.Segment
import coordinate.isInCircle
import util.circleintersection.CircleCircleIntersection
import util.circleintersection.CircleCircleIntersection.Type.COINCIDENT
import util.circleintersection.CircleCircleIntersection.Type.CONCENTRIC_CONTAINED
import util.circleintersection.CircleCircleIntersection.Type.ECCENTRIC_CONTAINED
import util.circleintersection.CircleCircleIntersection.Type.INTERNALLY_TANGENT
import util.circleintersection.CircleCircleIntersection.Type.OVERLAPPING
import util.circleintersection.LCircle
import util.circleintersection.LVector2
import util.iterators.zipWithSiblingsCyclical
import util.numbers.squared
import kotlin.math.sqrt

sealed class IntersectionData {
  class Overlapping(val points: Pair<Point, Point>) : IntersectionData()
  object Coincident : IntersectionData()
  object Contained : IntersectionData()
  object Container : IntersectionData()
  object SeparateOrExternallyTangent : IntersectionData()
}

fun LVector2.toPoint() = Point(x, y)
fun LCircle.toCirc() = Circ(c.toPoint(), r)

fun LCircle.intersection(other: LCircle): IntersectionData {
  val intersection = CircleCircleIntersection(this, other)

  return when (intersection.type) {
    OVERLAPPING -> Overlapping(
      Pair(
        intersection.intersectionPoint1.toPoint(),
        intersection.intersectionPoint2.toPoint(),
      ),
    )
    COINCIDENT -> Coincident
    CONCENTRIC_CONTAINED, ECCENTRIC_CONTAINED, INTERNALLY_TANGENT -> if (r > other.r) IntersectionData.Container else IntersectionData.Contained
    else -> SeparateOrExternallyTangent
  }
}

fun Circ.intersection(other: Circ): Arc =
  when (val intersection = toLCircle().intersection(other.toLCircle())) {
    is Overlapping -> {
      val (angle1, angle2) = intersection.points

      listOf(Arc(angle1, angle2, this), Arc(angle2, angle1, this))
        .find { arc -> !arc.pointAtBisector.isInCircle(other) }
        ?: throw Exception("Arc could not be found of intersection between $this, $other")
    }
    Coincident, IntersectionData.Contained -> Arc(Deg(0), 0.0, this)
    IntersectionData.Container, SeparateOrExternallyTangent -> Arc(this)
  }

// From S.O.: https://stackoverflow.com/a/13055116
fun getCircleLineIntersectionPoint(
  l: Segment,
  circ: Circ,
): List<Point> {
  val pointDiff = l.p2 - l.p1
  val centerToP1 = circ.origin - l.p1

  val a = pointDiff.x.squared() + pointDiff.y.squared()
  val bBy2 = pointDiff.x * centerToP1.x + pointDiff.y * centerToP1.y
  val c = centerToP1.x.squared() + centerToP1.y.squared() - circ.radius.squared()
  val pBy2 = bBy2 / a
  val q = c / a
  val disc = pBy2 * pBy2 - q
  if (disc < 0) return emptyList()

  // if disc == 0 ... dealt with later
  val tmpSqrt = sqrt(disc)
  val abScalingFactor1 = -pBy2 + tmpSqrt
  val abScalingFactor2 = -pBy2 - tmpSqrt
  val p1 = l.p1 - pointDiff * abScalingFactor1

  if (disc == 0.0) { // abScalingFactor1 == abScalingFactor2
    return listOf(p1)
  }

  val p2 = l.p1 - pointDiff * abScalingFactor2
  return listOf(p1, p2)
}

private fun isTangentIntersection(
  intersectionAndSurroundingPoints: Triple<Point, Point, Point>,
  c: Circ,
  r: BoundRect
): Boolean {
  val a1 = Arc(intersectionAndSurroundingPoints.first, intersectionAndSurroundingPoints.second, c)
  val a2 = Arc(intersectionAndSurroundingPoints.second, intersectionAndSurroundingPoints.third, c)

  val bothArcsOutsideRect = !r.contains(a1.pointAtBisector) && !r.contains(a2.pointAtBisector)
  val bothArcsInsideRect = r.contains(a1.pointAtBisector) && r.contains(a2.pointAtBisector)

  return bothArcsOutsideRect || bothArcsInsideRect
}

fun Circ.splitIntoArcsWhereIntersects(rect: BoundRect): List<Arc> {
  val intersectionPoints = mutableListOf<Point>()
  rect.segments.forEach {
    intersectionPoints.addAll(getCircleLineIntersectionPoint(it, this))
  }

  if (intersectionPoints.isEmpty()) {
    return listOf(Arc(this))
  }

  return intersectionPoints
    // distinct in case a corner is counted twice
    .distinct()
    .sortedBy { p -> angleAtPoint(p).value }
    .zipWithSiblingsCyclical()
    .filterNot { isTangentIntersection(it, this, rect) }
    .map { it.second }
    .zipWithSiblingsCyclical()
    .map { Arc(it.first, it.second, this) }
    .sortedBy { it.startDeg.value }
    .ifEmpty { listOf(Arc(this)) }
}

fun Circ.clipCircInsideRect(rect: BoundRect): List<Arc> =
  splitIntoArcsWhereIntersects(rect)
    .filter { rect.contains(it.pointAtBisector) }

fun Circ.clipCircOutsideRect(rect: BoundRect): List<Arc> =
  splitIntoArcsWhereIntersects(rect)
    .filterNot { rect.contains(it.pointAtBisector) }

fun Arc.clipInsideRect(rect: BoundRect): List<Arc> =
  clipCircInsideRect(rect)
    .map(this::getOverlap)
    .flatten()

fun Arc.clipOutsideRect(rect: BoundRect): List<Arc> =
  clipCircOutsideRect(rect)
    .map(this::getOverlap)
    .flatten()
