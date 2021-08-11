package util.image.opencvMat

import coordinate.Point
import coordinate.Segment

const val MinBinarySearchDistance = 1

private fun Segment.binarySearchHelper(predicate: (Segment) -> Boolean): Point? {
  val midPoint = midPoint
  if (length < MinBinarySearchDistance) return midPoint
  val firstHalf = Segment(p1, midPoint)
  if (predicate(firstHalf)) return firstHalf.binarySearchHelper(predicate)
  val secondHalf = Segment(midPoint, p2)
  if (predicate(secondHalf)) return secondHalf.binarySearchHelper(predicate)

  return null
}

fun Segment.binarySearchForBoundary(inBoundaryPredicate: (Point) -> Boolean): Point? {
  if (inBoundaryPredicate(p1) == inBoundaryPredicate(p2)) return null
  return binarySearchHelper { segment ->
    inBoundaryPredicate(segment.p1) != inBoundaryPredicate(segment.p2)
  }
}
