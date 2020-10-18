package appletExtensions

import appletExtensions.IntersectionData.Coincident
import appletExtensions.IntersectionData.Overlapping
import appletExtensions.IntersectionData.SeparateOrExternallyTangent
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import coordinate.isInCircle
import util.circleintersection.CircleCircleIntersection
import util.circleintersection.CircleCircleIntersection.Type.COINCIDENT
import util.circleintersection.CircleCircleIntersection.Type.CONCENTRIC_CONTAINED
import util.circleintersection.CircleCircleIntersection.Type.ECCENTRIC_CONTAINED
import util.circleintersection.CircleCircleIntersection.Type.INTERNALLY_TANGENT
import util.circleintersection.CircleCircleIntersection.Type.OVERLAPPING
import util.circleintersection.LCircle
import util.circleintersection.LVector2

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
    OVERLAPPING -> Overlapping(Pair(intersection.intersectionPoint1.toPoint(), intersection.intersectionPoint2.toPoint()))
    COINCIDENT -> Coincident
    CONCENTRIC_CONTAINED, ECCENTRIC_CONTAINED, INTERNALLY_TANGENT -> if (r > other.r) IntersectionData.Container else IntersectionData.Contained
    else -> SeparateOrExternallyTangent
  }
}

fun Circ.intersection(other: Circ): Arc = when (val intersection = toLCircle().intersection(other.toLCircle())) {
  is Overlapping -> {
    val (angle1, angle2) = intersection.points

    listOf(Arc(angle1, angle2, this), Arc(angle2, angle1, this))
      .find { arc -> !arc.pointAtBisector.isInCircle(other) }
      ?: throw Exception("Arc could not be found of intersection between $this, $other")
  }
  Coincident, IntersectionData.Contained -> Arc(Deg(0), 0f, this)
  IntersectionData.Container, SeparateOrExternallyTangent -> Arc(this)
}
