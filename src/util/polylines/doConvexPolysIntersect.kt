package util.polylines

import coordinate.Point
import util.iterators.every
import util.iterators.minMaxByOrNull

// Bulk of this logic is from https://github.com/JoelEager/Kotlin-Collision-Detector/blob/master/src/sat.kt
// just converted to use my data types
fun doConvexPolysIntersect(poly1: PolyLine, poly2: PolyLine): Boolean {
  if (poly1.isEmpty() || poly2.isEmpty()) return false

  return poly1.indices.every { i -> doesEdgeOverlap(poly1, poly2, i) } &&
    poly2.indices.every { i -> doesEdgeOverlap(poly2, poly1, i) }
}

fun doesEdgeOverlap(poly1: PolyLine, poly2: PolyLine, poly1Index: Int): Boolean {
  val orthogonalEdge =
    edgePoint(poly1[poly1Index], poly1[(poly1Index + 1) % poly1.size]).orthogonal()

  return overlap(project(poly1, orthogonalEdge), project(poly2, orthogonalEdge))
}

/**
 * Returns a vector going from point1 to point2
 */
fun edgePoint(point1: Point, point2: Point) = Point(point2.x - point1.x, point2.y - point1.y)

/**
 * Returns a vector showing how much of the poly lies along the axis
 */
fun project(poly: PolyLine, axis: Point): Point =
  poly.minMaxByOrNull {
    it.dot(axis)
  }?.let {
    Point(it.start, it.endInclusive)
  } ?: Point.Zero

/**
 * Returns a boolean indicating if the two projections overlap
 */
fun overlap(projection1: Point, projection2: Point) = projection1.x <= projection2.y &&
  projection2.x <= projection1.y
