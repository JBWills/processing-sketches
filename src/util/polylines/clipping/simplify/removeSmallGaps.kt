package util.polylines.clipping.simplify

import util.polylines.MutablePolyLine
import util.polylines.PolyLine

fun List<PolyLine>.removeSmallGaps(minDist: Double = 5.0): List<PolyLine> {
  val result = mutableListOf<MutablePolyLine>()

  stream()
    .filter(PolyLine::isNotEmpty)
    .forEach { line ->

      val lastPointOnPrevious = result.lastOrNull()?.lastOrNull()

      if (lastPointOnPrevious == null) {
        result.add(line.toMutableList())
      } else {
        val firstOnCurr = line.first()
        if (firstOnCurr == lastPointOnPrevious) {
          result.last().addAll(line.slice(1 until line.size))
        }
        val dist = lastPointOnPrevious.dist(firstOnCurr)

        if (dist < minDist) {
          result.last().addAll(line)
        } else {
          result.add(line.toMutableList())
        }
      }
    }

  return result
}
