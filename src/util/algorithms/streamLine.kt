package util.algorithms

import com.google.common.collect.Queues
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import org.locationtech.jts.geom.Envelope
import util.base.DoubleRange
import util.iterators.skipFirst
import util.polylines.PolyLine
import util.polylines.iterators.walk
import util.polylines.toSegments
import util.quadTree.GQuadtree
import util.random.randPoint
import kotlin.random.Random

private fun Point.toEnvelope(dist: Double) = Envelope(
  x - dist * 0.5,
  x + dist * 0.5,
  y - dist * 0.5,
  y + dist * 0.5,
)

private fun GQuadtree<Point>.containsPointAtDist(point: Point, dist: Double) =
  query(point.toEnvelope(dist)).any { it.dist(point) < dist }


private fun Point.generateFlowLine(
  bound: BoundRect,
  lengthRange: DoubleRange,
  step: Double,
  pointTree: GQuadtree<Point>,
  dTest: Double,
  get: (Point) -> Deg?
): PolyLine? {
  val resultLineForwards = mutableListOf<Point>()
  val resultLineBackwards = mutableListOf<Point>()

  fun Point.getNextPoint(
    backwards: Boolean
  ): Point? {
    val direction = get(this)?.let { if (backwards) it + 180 else it } ?: return null
    val newPoint = this + direction.unitVector * step

    if (!bound.contains(newPoint) || pointTree.containsPointAtDist(newPoint, dTest)) {
      return null
    }

    return newPoint
  }

  var lastPointForwards: Point? = this
  var lastPointBackwards: Point? = this
  var length = 0.0

  while (lastPointForwards != null || lastPointBackwards != null) {
    if (length > lengthRange.endInclusive) {
      break
    }

    if (lastPointForwards != null) {
      length += step
      resultLineForwards.add(lastPointForwards)
      lastPointForwards = lastPointForwards.getNextPoint(backwards = false)
    }

    if (lastPointBackwards != null) {
      if (lastPointBackwards != lastPointForwards) {
        length += step
      }
      resultLineBackwards.add(lastPointBackwards)
      lastPointBackwards = lastPointBackwards.getNextPoint(backwards = true)
    }
  }


  val resultLine = resultLineBackwards.skipFirst().reversed().plus(resultLineForwards)
  if (length < lengthRange.start || resultLine.size <= 1) {
    return null
  }

  return resultLine
}

fun streamLines(
  seed: Int,
  bound: BoundRect,
  distance: Double,
  lengthRange: DoubleRange,
  step: Double,
  dTest: Double = distance * 0.5,
  get: (Point) -> Deg?
): List<PolyLine> {
  val r = Random(seed)

  val pointTree = GQuadtree<Point> { toEnvelope(dTest) }
  val resultLines = mutableListOf<PolyLine>()
  val queue = Queues.newArrayDeque<PolyLine>()

  fun Point.toFlowLine(minDist: Double = lengthRange.start): PolyLine? =
    generateFlowLine(
      bound = bound,
      lengthRange = minDist..lengthRange.endInclusive,
      step = step,
      pointTree = pointTree,
      dTest = dTest,
      get = get,
    )

  fun addNewLine(line: PolyLine) {
    val sampledLine = line.walk(distance)
    queue.add(sampledLine)
    pointTree.insertAll(sampledLine)
    resultLines.add(line)
  }

  val initialLine =
    r.randPoint(bound).toFlowLine(minDist = 0.0)
    // try one more time, why not?
      ?: r.randPoint(bound).toFlowLine(minDist = 0.0)
      ?: return listOf()
  addNewLine(initialLine)

  while (queue.isNotEmpty()) {
    val line = queue.pop()
    val possibleFlowLines = line.toSegments().shuffled(r).flatMap { segment ->
      listOf(1.0, -1.0).mapNotNull { multiplier ->
        val point = (segment.p1 + segment.slope.perpendicular.unitVector * multiplier * distance)
        point.toFlowLine()
      }
    }

    possibleFlowLines.sortedByDescending { it.size }.map { flowLine ->
      if (!flowLine.walk(distance).any { p ->
          pointTree.containsPointAtDist(p, dTest)
        }) {
        addNewLine(flowLine)
      }
    }

  }

  return resultLines
}
