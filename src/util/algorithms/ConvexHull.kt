package util.algorithms

import coordinate.Point
import util.algorithms.PointOrientation.Clockwise
import util.algorithms.PointOrientation.Colinear
import util.algorithms.PointOrientation.CounterClockwise
import java.util.*


// from: https://www.nayuki.io/res/convex-hull-algorithm/ConvexHull.java
// Returns a new list of points representing the convex hull of
// the given set of points. The convex hull excludes collinear points.
// This algorithm runs in O(n log n) time.
fun Iterable<Point>.makeHull(): List<Point> = makeHullPresorted(sorted())

// from: https://www.nayuki.io/res/convex-hull-algorithm/ConvexHull.java
// Returns the convex hull, assuming that each points[i] <= points[i + 1]. Runs in O(n) time.
fun makeHullPresorted(points: List<Point>): List<Point> {
  if (points.size <= 1) return points.toList()

  // Andrew's monotone chain algorithm. Positive y coordinates correspond to "up"
  // as per the mathematical convention, instead of "down" as per the computer
  // graphics convention. This doesn't affect the correctness of the result.
  val upperHull: MutableList<Point> = ArrayList()
  for (p in points) {
    while (upperHull.size >= 2) {
      val q = upperHull[upperHull.size - 1]
      val r = upperHull[upperHull.size - 2]
      if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
        upperHull.removeAt(upperHull.size - 1) else break
    }
    upperHull.add(p)
  }
  upperHull.removeAt(upperHull.size - 1)
  val lowerHull: MutableList<Point> = ArrayList()
  for (i in points.indices.reversed()) {
    val p = points[i]
    while (lowerHull.size >= 2) {
      val q = lowerHull[lowerHull.size - 1]
      val r = lowerHull[lowerHull.size - 2]
      if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
        lowerHull.removeAt(lowerHull.size - 1) else break
    }
    lowerHull.add(p)
  }
  lowerHull.removeAt(lowerHull.size - 1)
  if (!(upperHull.size == 1 && upperHull == lowerHull)) upperHull.addAll(lowerHull)

  return upperHull
}

private enum class PointOrientation {
  Colinear,
  Clockwise,
  CounterClockwise
}

fun Set<Point>.convexHull(): List<Point> {
  if (size < 3) return listOf()

  val hull = mutableListOf<Point>()

  val leftmostPoint = minByOrNull { it.x } ?: return listOf()

  var p: Point = leftmostPoint
  do {
    hull.add(p)

    p = minus(p).reduce { acc, point ->
      if (orientation(p, point, acc) == Clockwise) acc
      else point
    }
  } while (p != leftmostPoint)

  if (hull.last() != hull.first()) hull.add(hull.last())

  return hull
}

/**
 * From: https://www.geeksforgeeks.org/convex-hull-set-1-jarviss-algorithm-or-wrapping/
 * To find orientation of ordered triplet (p, q, r).
 * The function returns following values
 * 0 --> p, q and r are colinear
 * 1 --> Clockwise
 * 2 --> Counterclockwise
 */
private fun orientation(p: Point, q: Point, r: Point): PointOrientation {
  val v = (
    (q.y - p.y) * (r.x - q.x) -
      (q.x - p.x) * (r.y - q.y)
    ).toInt()

  return when {
    v == 0 -> Colinear
    v > 0 -> Clockwise
    else -> CounterClockwise
  }
}
