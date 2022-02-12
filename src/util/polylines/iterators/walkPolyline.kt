@file:Suppress("unused")

package util.polylines

import coordinate.Point
import coordinate.Segment
import util.iterators.PolyLineIterator
import util.iterators.secondOrNull
import util.iterators.secondToLast
import util.polylines.iterators.forEachSegmentIndexed
import kotlin.math.abs

/**
 * @param step must be positive
 */
private fun <T> walkWithPercentAndSegmentForwardsOnly(
  line: PolyLine,
  step: Double,
  block: (index: Int, percent: Double, segment: Segment, point: Point) -> T,
): List<T> {
  if (step <= 0.0) {
    throw Exception("Cannot call walkWithPercentAndSegmentForwardsOnly with a step <= 0.0")
  }

  val totalLength = line.length

  if (line.isEmpty()) return listOf()

  val firstItemProcessed = block(
    0,
    0.0,
    Segment(line.first(), line.secondOrNull() ?: line.first()),
    line.first(),
  )
  if (line.size < 2 || totalLength == 0.0) return listOf(firstItemProcessed)

  var lengthSoFar = 0.0
  var lastSegmentUnused = 0.0

  val walkedLine = mutableListOf(firstItemProcessed)
  line.forEachSegmentIndexed { index, segment ->
    val segmentEndLength = lengthSoFar + segment.length

    var currLength = lengthSoFar + step - lastSegmentUnused

    val pointsOnSegment = mutableListOf<T>()
    val pointsOnSegmentUntransformed = mutableListOf<Point>()
    while (currLength < segmentEndLength) {
      val p = segment.getPointAtDist(currLength - lengthSoFar)
      val transformedP = block(index, currLength / totalLength, segment, p)
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

    walkedLine.addAll(pointsOnSegment)
  }

  if (lastSegmentUnused > 0.0) {
    walkedLine.add(
      block(line.size - 1, 1.0, Segment(line.secondToLast(), line.last()), line.last()),
    )
  }

  return walkedLine
}

private fun walkWithPercentAndSegmentForwardsOnly(
  line: PolyLine,
  step: Double,
): PolyLine = walkWithPercentAndSegmentForwardsOnly(line, step) { _, _, _, point -> point }

fun <T> PolyLine.walkWithPercentAndSegment(
  step: Number,
  block: (percent: Double, segment: Segment, point: Point) -> T
): List<T> {
  if (step.toDouble() == 0.0) {
    throw Exception("Can't walk along a line with a step of 0, it will be infinite!")
  }

  val line = if (step.toDouble() < 0.0) reversed() else this

  @Suppress("NAME_SHADOWING")
  val step = abs(step.toDouble())

  return walkWithPercentAndSegmentForwardsOnly(line, step) { _, percent, segment, point ->
    block(percent, segment, point)
  }
}

fun <T> PolyLine.walkWithPercentAndSegmentIndexed(
  step: Number,
  block: (index: Int, percent: Double, segment: Segment, point: Point) -> T
): List<T> {
  if (step.toDouble() == 0.0) {
    throw Exception("Can't walk along a line with a step of 0, it will be infinite!")
  }

  val line = if (step.toDouble() < 0.0) reversed() else this

  @Suppress("NAME_SHADOWING")
  val step = abs(step.toDouble())

  return walkWithPercentAndSegmentForwardsOnly(line, step, block)
}

fun PolyLine.walkWithPercentAndSegment(step: Number): List<Point> =
  walkWithPercentAndSegment(step) { _, _, point -> point }

fun <T> PolyLine.walk(step: Number, block: (point: Point) -> T): List<T> =
  walkWithPercentAndSegment(step) { _, _, point -> block(point) }

fun PolyLine.walk(step: Number): List<Point> = walk(step) { it }

fun PolyLine.chunkedByDistance(step: Number): List<PolyLine> {
  val iterator = PolyLineIterator(this)

  val result: MutableList<PolyLine> = mutableListOf()

  while (!iterator.atEnd()) {
    result.add(iterator.move(step.toDouble()).points)
  }

  return result
}
