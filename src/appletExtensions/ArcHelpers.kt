package appletExtensions

import coordinate.Arc
import coordinate.Circ
import coordinate.Point
import coordinate.isInCircle
import util.circleintersection.CircleCircleIntersection
import util.circleintersection.LCircle
import util.circleintersection.LVector2
import util.map

fun LVector2.toPoint() = Point(x, y)

fun LCircle.intersection(other: LCircle): Pair<Point, Point>? {
  val intersection = CircleCircleIntersection(this, other)
  if (intersection.type == CircleCircleIntersection.Type.OVERLAPPING) {
    return Pair(intersection.intersectionPoint1.toPoint(), intersection.intersectionPoint2.toPoint())
  }

  return null
}

fun Circ.intersection(other: Circ): Arc {
  val (angle1, angle2) = toLCircle()
    .intersection(other.toLCircle())
    ?.map(this::angleAtPoint)
    ?: return Arc(this)

  return listOf(Arc(angle1, angle2, this), Arc(angle2, angle1, this))
    .find { arc -> !arc.pointAtBisector.isInCircle(other) }
    ?: throw Exception("Arc could not be found of intersection between $this, $other")
}
