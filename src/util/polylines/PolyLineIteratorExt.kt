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

fun PolyLine.walkWithPercentAndSegment(
  step: Number,
  block: (percent: Double, segment: Segment, point: Point) -> Point = { _, _, point -> point }
): List<Point> {
  val line = if (step.toDouble() < 0.0) reversed() else this

  @Suppress("NAME_SHADOWING")
  val step = abs(step.toDouble())

  val totalLength = length

  if (size < 1) return listOf()
  if (size < 2 || totalLength == 0.0) return listOf(block(0.0, Segment(first(), first()), first()))

  var lengthSoFar = 0.0
  var lastSegmentUnused = 0.0

  val walkedLine = block(0.0, Segment(first(), this[1]), first()) + line.mapBySegment { segment ->
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

fun PolyLine.walk(step: Number, block: (point: Point) -> Point = { it }): List<Point> =
  walkWithPercentAndSegment(step) { _, _, point ->
    block(point)
  }
