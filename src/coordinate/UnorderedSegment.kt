package coordinate

import util.polylines.sortClockwise

data class UnorderedSegment(val p1: Point, val p2: Point) {
  override fun equals(other: Any?): Boolean {
    if (other == null) {
      return false
    }

    if (other !is UnorderedSegment) {
      return false
    }
    val otherPair = Pair(other.p1, other.p2)
    return (otherPair == Pair(p1, p2) || otherPair == Pair(p2, p1))
  }

  fun toSegment(): Segment = Segment(p1, p2)

  override fun hashCode(): Int {
    val (sortedP1, sortedP2) = listOf(p1, p2).sortClockwise(Point.Zero)
    var result = sortedP1.hashCode()
    result = 31 * result + sortedP2.hashCode()
    return result
  }
}
