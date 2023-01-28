package util.voronoi

import com.github.ricardomatias.Delaunator
import coordinate.Circ
import coordinate.Cone2D
import coordinate.Line
import coordinate.Point
import coordinate.Ray
import coordinate.Segment
import coordinate.UnorderedSegment
import util.iterators.mapWithSurroundingCyclical
import util.numbers.bound
import util.numbers.floorInt
import util.numbers.squared
import util.polylines.PolyLine
import util.polylines.bounds
import util.polylines.closed
import util.polylines.sortClockwise
import util.polylines.toPolyLine
import util.polylines.translated
import util.tuple.map
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sin


data class Triangle(val pa: Point, val pb: Point, val pc: Point) {
  constructor(points: List<Point>) : this(points[0], points[1], points[2])

  val points get() = listOf(pa, pb, pc)
  val a = pc.dist(pb)
  val b = pa.dist(pc)
  val c = pa.dist(pb)

  private val aSq = a.squared()
  private val bSq = b.squared()
  private val cSq = c.squared()

  private val cosA = (bSq + cSq - aSq) / (2 * b * c)
  private val cosB = (cSq + aSq - bSq) / (2 * c * a)
  private val cosC = (aSq + bSq - cSq) / (2 * a * b)

  private val radA = acos(cosA.bound(-1.0..1.0))
  private val radB = acos(cosB.bound(-1.0..1.0))
  private val radC = acos(cosC.bound(-1.0..1.0))

  private val circumcenter = run {
    val sin2A = sin(2 * radA)
    val sin2B = sin(2 * radB)
    val sin2C = sin(2 * radC)
    (pa * sin2A + pb * sin2B + pc * sin2C) / (sin2A + sin2B + sin2C)
  }

  val circumcircle = Circ(
    circumcenter,
    points.maxOfOrNull { circumcenter.dist(it) } ?: 1,
  )

  fun getThetaAtPoint(p: Point): Double = when (p) {
    pa -> radA
    pb -> radB
    pc -> radC
    else -> throw Exception("Point does not exist on triangle. Point: $p. Triangle: $this.")
  }

  fun getOtherPoints(p: Point): Pair<Point, Point> = when (p) {
    pa -> pb to pc
    pb -> pa to pc
    else -> pb to pa
  }

  fun toPolyLine(): PolyLine = listOf(pa, pb, pc, pa)

  private fun hasPoint(p: Point) = p == pa || p == pb || p == pc

  fun containsEdge(edge: UnorderedSegment) =
    hasPoint(edge.p1) && hasPoint(edge.p2)

  fun unorderedSegments() = listOf(
    UnorderedSegment(pa, pb),
    UnorderedSegment(pb, pc),
    UnorderedSegment(pc, pa),
  )

  fun sharesAPoint(tri: Triangle): Boolean =
    points.any { thisPoint -> tri.hasPoint(thisPoint) }

  override fun toString(): String {
    fun p(p: Point) = "(x=${p.x.floorInt()}, y=${p.y.floorInt()})"
    return "Triangle(a=${p(pa)}, b=${p(pb)}, c=${p(pc)})"
  }
}

fun getSuperTriangle(points: List<Point>): Triangle = points.bounds.let { pointBounds ->
  val trianglePointsAroundOrigin = listOf(
    Point.Zero,
    Point(2 * pointBounds.width, 0),
    Point(0, 2 * pointBounds.height),
  )
  Triangle(trianglePointsAroundOrigin.translated(pointBounds.topLeft))
}

fun triangulate(points: List<Point>): List<Triangle> {
  val pointsArr = DoubleArray(points.size * 2)
  points.forEachIndexed { index, point ->
    pointsArr[index * 2] = point.x
    pointsArr[index * 2 + 1] = point.y
  }

  val trianglesArr = Delaunator(pointsArr).triangles

  val triangles = mutableListOf<Triangle>()
  for (i in trianglesArr.indices step 3) {
    val t0 = trianglesArr[i] * 2
    val t1 = trianglesArr[i + 1] * 2
    val t2 = trianglesArr[i + 2] * 2

    // clockwise point orientation
    val p1 = Point(pointsArr[t0], pointsArr[t0 + 1])
    val p2 = Point(pointsArr[t1], pointsArr[t1 + 1])
    val p3 = Point(pointsArr[t2], pointsArr[t2 + 1])

    triangles.add(Triangle(p1, p2, p3))
  }

  return triangles
}

fun voronoi(points: List<Point>): List<PolyLine> {
  val triangles = triangulate(points)

  val pointsToTriangles: MutableMap<Point, MutableList<Triangle>> = mutableMapOf()

  triangles.forEach { tri ->
    tri.points.map { p ->
      val newList = pointsToTriangles.getOrDefault(p, mutableListOf())
      newList.add(tri)
      pointsToTriangles.put(p, newList)
    }
  }

  val polys = mutableListOf<PolyLine>()
  points.forEach { point ->
    val surroundingTris = pointsToTriangles[point]

    val unorderedPoints = mutableSetOf<Point>()

    surroundingTris?.forEach {
      val pair = it.getOtherPoints(point)
      unorderedPoints.add(pair.first)
      unorderedPoints.add(pair.second)
    }

    val sortedPoints = unorderedPoints.toList().sortClockwise(point)

    if (sortedPoints.isNotEmpty()) {
      polys.add(sortedPoints + sortedPoints[0])
    }
  }

  return polys
}

fun getBisectors(points: List<Point>, tris: List<Triangle>): List<List<Line>> {
  val pointsToAdjacentPoints: MutableMap<Point, MutableSet<Point>> = mutableMapOf()
  val pointsToAdjacentTris: MutableMap<Point, MutableSet<Triangle>> = mutableMapOf()

  tris.forEach { tri ->
    tri.points.forEach { p ->
      pointsToAdjacentPoints.putIfAbsent(p, mutableSetOf())
      pointsToAdjacentPoints[p]?.addAll(tri.points)
      pointsToAdjacentTris.putIfAbsent(p, mutableSetOf())
      pointsToAdjacentTris[p]?.add(tri)
    }
  }

  return points.mapNotNull { point ->
    val surroundingPoints = pointsToAdjacentPoints[point] ?: return@mapNotNull null
    surroundingPoints.remove(point)

    val sumTheta = (pointsToAdjacentTris[point] ?: return@mapNotNull null).toList()
      .sumOf { it.getThetaAtPoint(point) }

    // This means the point is on the edge of the hull
    if (sumTheta < 1.9 * PI) {
      return@mapNotNull null
    }

    surroundingPoints
      .toList()
      .sortClockwise(point)
      .map {
        Segment(point, it)
          .perpendicularBisector()
      }
  }
}

fun voronoi2(points: List<Point>): List<PolyLine> {
  val triangles = triangulate(points)

  return getBisectors(points, triangles).map { bisectors ->
    bisectors.mapWithSurroundingCyclical { prev, curr, next ->
      val intersectionWithPrev = curr.intersection(prev) ?: return@mapWithSurroundingCyclical null
      val intersectionWithNext = curr.intersection(next) ?: return@mapWithSurroundingCyclical null
      intersectionWithPrev.lineTo(intersectionWithNext)
    }.filterNotNull().toPolyLine().closed()
  }
}

fun intersectLinesSoRayConeContainsPoint(lines: Pair<Line, Line>, point: Point): Pair<Ray, Ray>? {
  val intersection = lines.first.intersection(lines.second) ?: return null

  val (firstLineSlopes, secondLineSlopes) = lines.map { line ->
    listOf(
      line.slope,
      line.slope + 180,
    )
  }

  firstLineSlopes.forEach { firstSlope ->
    secondLineSlopes.forEach { secondSlope ->
      val (ray1, ray2) = (firstSlope to secondSlope).map { Ray(intersection, it) }
      if (Cone2D.fromRays(ray1, ray2, isGreaterThan180 = false).contains(point)) {
        return ray1 to ray2
      }
    }
  }

  return null
}
