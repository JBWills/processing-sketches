package util.polylines.iterators

import util.iterators.skipFirst
import util.polylines.PolyLine

fun List<PolyLine>.mergeConsecutiveIfConnected(): List<PolyLine> {
  val result = mutableListOf<PolyLine>()

  var last: PolyLine? = null

  forEach { polyLine ->
    last?.let { lastNonNull ->
      if (lastNonNull.isEmpty() || polyLine.isEmpty() || lastNonNull.last() == polyLine.first()) {
        last = lastNonNull.plus(polyLine.skipFirst())
      } else {
        result.add(lastNonNull)
      }
    }

    last = polyLine
  }


  last?.let { result.add(it) }

  return result
}
