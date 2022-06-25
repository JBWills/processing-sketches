@file:Suppress("unused")

package util.polylines.iterators

import coordinate.Point
import coordinate.Segment
import util.iterators.secondOrNull
import util.iterators.secondToLast
import util.polylines.PolyLine
import util.polylines.length
import kotlin.math.abs

data class WalkCursor(
  val index: Int,
  val pointIndex: Int,
  val percent: Double,
  val distance: Double,
  val point: Point,
  val segment: Segment
) {
  val normal get() = segment.normal(clockWise = true)
}

/**
 * @param step must be positive
 */
inline fun <T> walkWithCursorForwardsOnly(
  line: PolyLine,
  step: Double,
  crossinline block: (WalkCursor) -> T,
): List<T> {
  if (step <= 0.0) {
    throw Exception("Cannot call walkWithCursorForwardsOnly with a step <= 0.0")
  }

  val totalLength = line.length

  if (line.isEmpty()) return listOf()

  val firstItemProcessed = block(
    WalkCursor(
      index = 0,
      pointIndex = 0,
      percent = 0.0,
      distance = 0.0,
      segment = Segment(line.first(), line.secondOrNull() ?: line.first()),
      point = line.first(),
    ),
  )
  if (line.size < 2 || totalLength == 0.0) return listOf(firstItemProcessed)

  var lengthSoFar = 0.0
  var lastSegmentUnused = 0.0

  val walkedLine = mutableListOf(firstItemProcessed)
  var index = 1
  line.forEachSegmentIndexed { pointIndex, segment ->
    val segmentEndLength = lengthSoFar + segment.length

    var currLength = lengthSoFar + step - lastSegmentUnused

    val pointsOnSegment = mutableListOf<T>()
    val pointsOnSegmentUntransformed = mutableListOf<Point>()
    while (currLength < segmentEndLength) {
      val p = segment.getPointAtDist(currLength - lengthSoFar)
      val transformedP = block(
        WalkCursor(
          index = index,
          pointIndex = pointIndex,
          distance = currLength,
          percent = currLength / totalLength,
          segment = segment,
          point = p,
        ),
      )
      pointsOnSegment.add(transformedP)
      pointsOnSegmentUntransformed.add(p)
      currLength += step
      index++
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
      block(
        WalkCursor(
          index = index,
          pointIndex = line.size - 1,
          distance = totalLength,
          percent = 1.0,
          segment = Segment(line.secondToLast(), line.last()),
          point = line.last(),
        ),
      ),
    )
  }

  return walkedLine
}

inline fun <T> PolyLine.walkWithCursor(
  step: Number,
  crossinline block: (WalkCursor) -> T
): List<T> {
  if (step.toDouble() == 0.0) {
    throw Exception("Can't walk along a line with a step of 0, it will be infinite!")
  }

  val line = if (step.toDouble() < 0.0) reversed() else this

  @Suppress("NAME_SHADOWING")
  val step = abs(step.toDouble())

  return walkWithCursorForwardsOnly(line, step) { cursor -> block(cursor) }
}

fun PolyLine.walkWithCursor(step: Number): List<Point> =
  walkWithCursor(step) { it.point }

fun <T> PolyLine.walk(step: Number, block: (point: Point) -> T): List<T> =
  walkWithCursor(step) { block(it.point) }

fun PolyLine.walk(step: Number): List<Point> = walk(step) { it }
