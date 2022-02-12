package util.polylines.iterators

import coordinate.Segment
import util.iterators.forEachSibling
import util.iterators.forEachSiblingIndexed
import util.iterators.mapWithSibling
import util.iterators.mapWithSiblingIndexed
import util.polylines.PolyLine

fun PolyLine.forEachSegment(block: (Segment) -> Unit) = forEachSibling { curr, next ->
  block(Segment(curr, next))
}

fun PolyLine.forEachSegmentIndexed(block: (Int, Segment) -> Unit) =
  forEachSiblingIndexed { index, curr, next ->
    block(index, Segment(curr, next))
  }

fun <R> PolyLine.mapBySegment(block: (Segment) -> R): List<R> =
  mapWithSibling { curr, next -> block(Segment(curr, next)) }

fun <R> PolyLine.mapBySegmentIndexed(block: (Int, Segment) -> R): List<R> =
  mapWithSiblingIndexed { index, curr, next -> block(index, Segment(curr, next)) }

fun <R> PolyLine.flatMapSegments(block: (Segment) -> List<R>): List<R> =
  mapBySegment(block).flatten()
