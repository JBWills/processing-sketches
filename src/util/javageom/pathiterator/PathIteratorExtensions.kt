package util.javageom.pathiterator

import coordinate.Point
import util.javageom.pathiterator.PathSegmentType.Companion.asPathSegmentType
import java.awt.geom.PathIterator

data class SegmentResponse(val type: PathSegmentType, val points: List<Point>)

fun PathIterator.segmentTypeAndPoints(): SegmentResponse {
  val pointArr = DoubleArray(6)
  val segmentType = currentSegment(pointArr).asPathSegmentType()
  return SegmentResponse(segmentType, segmentType.getPoints(pointArr))
}

fun PathIterator.forEach(block: (SegmentResponse) -> Unit) {
  while (!isDone) {
    block(segmentTypeAndPoints())
    next()
  }
}
