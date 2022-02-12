package util.iterators

import coordinate.Point
import coordinate.Segment
import util.base.DoubleRange
import util.numbers.ceilInt
import util.numbers.floorInt
import util.numbers.min
import util.polylines.PolyLine
import util.polylines.length

data class LineIteratorResult(val points: PolyLine, val index: Double)

class PolyLineIterator(val line: PolyLine) {
  private val totalDist by lazy { line.length }
  private var currDist = 0.0
  private var currIndex = 0.0


  private fun getAtDoubleIndex(index: Double): Point {
    return Segment(line[index.floorInt()], line[index.ceilInt()]).getPointAtPercent(index % 1)
  }

  fun atEnd(): Boolean =
    currDist == totalDist || currIndex == (line.size - 1).toDouble()

  private fun findIndexAfterMove(amount: Double): Double {
    fun getPercentAtDistance(segment: Segment, dist: Double): Double {
      val segmentDist = segment.length
      return dist / segmentDist
    }

    var index = currIndex

    var distLeft = amount

    while (index <= line.size - 2) {
      val nextIndex = (index + 1).floorInt().toDouble()
      val segment = Segment(getAtDoubleIndex(index), getAtDoubleIndex(nextIndex))

      if (distLeft - segment.length <= 0) {
        return index + getPercentAtDistance(segment, distLeft)
      }

      index = nextIndex
      distLeft -= segment.length
    }

    return line.size.toDouble() - 1
  }

  private fun getPointsBetweenIndices(indices: DoubleRange): PolyLine {
    val start = getAtDoubleIndex(indices.start)
    if (indices.start == indices.endInclusive) {
      return listOf(start)
    }

    val end = getAtDoubleIndex(indices.endInclusive)
    val startIntIndex = indices.start.ceilInt()
    val endIntIndex = indices.endInclusive.floorInt()

    val result = mutableListOf<Point>()

    if (startIntIndex.toDouble() != indices.start) {
      result.add(start)
    }

    (startIntIndex until endIntIndex).forEach { i ->
      result.add(line[i])
    }

    if (endIntIndex.toDouble() != indices.endInclusive) {
      result.add(end)
    }

    return result
  }

  fun move(amount: Double): LineIteratorResult {
    if (amount < 0) {
      throw Exception("Can't move backwards")
    }

    val oldIndex = currIndex
    currIndex = findIndexAfterMove(amount)
    currDist = min(currDist + amount, totalDist)
    
    return LineIteratorResult(
      points = getPointsBetweenIndices(oldIndex..currIndex),
      index = currIndex,
    )
  }
}
