@file:Suppress("unused")

package util.polylines

import coordinate.Point
import coordinate.Segment
import coordinate.transforms.ShapeTransform
import interfaces.shape.transform
import util.iterators.deepDeepMap
import util.iterators.deepMap
import util.iterators.forEachWithNext
import util.iterators.mapWithNext
import util.iterators.mapWithNextIndexed
import util.iterators.secondOrNull
import util.iterators.secondToLast
import kotlin.math.abs

fun PolyLine.mapWithLength(block: (point: Point, length: Double) -> Point): PolyLine {
  if (isEmpty()) return emptyList()

  var length = 0.0
  var lastPoint = first()
  return map { point ->
    length += Segment(point, lastPoint).length
    val newPoint = block(point, length)
    lastPoint = point
    newPoint
  }
}

fun PolyLine.forEachSegment(block: (Segment) -> Unit) = forEachWithNext { curr, next ->
  next?.let { block(Segment(curr, it)) }
}

fun <R> PolyLine.mapBySegment(block: (Segment) -> R): List<R> =
  mapWithNext { curr, next -> block(Segment(curr, next)) }

fun <R> PolyLine.mapBySegmentIndexed(block: (Int, Segment) -> R): List<R> =
  mapWithNextIndexed { index, curr, next -> block(index, Segment(curr, next)) }

fun PolyLine.flatMapSegments(block: (Segment) -> List<Segment>): List<Segment> =
  mapBySegment(block).flatten()

fun PolyLine.transform(t: ShapeTransform): PolyLine = map { it.transform(t) }

@JvmName("transformPolyLineList")
fun List<PolyLine>.transform(t: ShapeTransform): List<PolyLine> = deepMap { it.transform(t) }

@JvmName("transformPolyLineListList")
fun List<List<PolyLine>>.transform(t: ShapeTransform): List<List<PolyLine>> =
  deepDeepMap { it.transform(t) }

/**
 * @param step must be positive
 */
private fun walkWithPercentAndSegmentForwardsOnly(
  line: PolyLine,
  step: Double,
  block: (percent: Double, segment: Segment, point: Point) -> Point = { _, _, point -> point },
): List<Point> {
  if (step <= 0.0) {
    throw Exception("Cannot call walkWithPercentAndSegmentForwardsOnly with a step <= 0.0")
  }

  val totalLength = line.length

  if (line.isEmpty()) return listOf()

  val firstItemProcessed = block(
    0.0,
    Segment(line.first(), line.secondOrNull() ?: line.first()),
    line.first(),
  )
  if (line.size < 2 || totalLength == 0.0) return listOf(firstItemProcessed)

  var lengthSoFar = 0.0
  var lastSegmentUnused = 0.0

  val walkedLine = firstItemProcessed + line.mapBySegment { segment ->
    val segmentEndLength = lengthSoFar + segment.length

    var currLength = lengthSoFar + step - lastSegmentUnused

    val pointsOnSegment = mutableListOf<Point>()
    val pointsOnSegmentUntransformed = mutableListOf<Point>()
    while (currLength < segmentEndLength) {
      val p = segment.getPointAtDist(currLength - lengthSoFar)
      val transformedP = block(currLength / totalLength, segment, p)
      pointsOnSegment.add(transformedP)
      pointsOnSegmentUntransformed.add(p)
      currLength += step
    }

    if (pointsOnSegmentUntransformed.isNotEmpty()) {
      lastSegmentUnused = pointsOnSegmentUntransformed.last().dist(segment.p2)
    } else {
      lastSegmentUnused += segment.length
    }

    lengthSoFar += segment.length
    pointsOnSegment
  }.flatten()
    .toMutableList()

  if (lastSegmentUnused > 0.0) {
    walkedLine.add(block(1.0, Segment(line.secondToLast(), line.last()), line.last()))
  }

  return walkedLine
}

fun PolyLine.walkWithPercentAndSegment(
  step: Number,
  block: (percent: Double, segment: Segment, point: Point) -> Point = { _, _, point -> point }
): List<Point> {
  if (step.toDouble() == 0.0) {
    throw Exception("Can't walk along a line with a step of 0, it will be infinite!")
  }

  val line = if (step.toDouble() < 0.0) reversed() else this

  @Suppress("NAME_SHADOWING")
  val step = abs(step.toDouble())

  return walkWithPercentAndSegmentForwardsOnly(line, step, block)
}

fun PolyLine.walk(step: Number, block: (point: Point) -> Point = { it }): List<Point> =
  walkWithPercentAndSegment(step) { _, _, point ->
    block(point)
  }
