package coordinate

import coordinate.ContinuousPoints.Companion.splitInBounds
import coordinate.Point.Companion.plusIf
import util.forEachWithSurrounding
import util.mapWithSurroundingCyclical

fun List<Point>.isCyclical() = size > 2 && first() == last()

data class ContinuousPoints(val isInBound: Boolean, val points: MutableList<Point>) {
  val first get() = points.first()
  val last get() = points.last()
  val size get() = points.size

  fun combineWith(o: ContinuousPoints) = ContinuousPoints(isInBound, (points + o.points).toMutableList())

  companion object {
    val List<ContinuousPoints>.firstPoint get() = first().first
    val List<ContinuousPoints>.lastPoint get() = last().last

    fun BoundRect.splitInBounds(points: List<Point>): List<ContinuousPoints> {
      if (points.size < 2) return listOf()

      val isCyclical = points.isCyclical()
      val res = mutableListOf<ContinuousPoints>()

      points.forEachWithSurrounding { prev, p, _ ->
        val prevMaybeCyclical = prev ?: if (isCyclical) points.last() else null

        if (prevMaybeCyclical != p) {
          val currInBound = contains(p)
          if (res.isEmpty() || res.last().isInBound != currInBound) {
            res.add(ContinuousPoints(currInBound, mutableListOf(p)))
          } else {
            res.last().points.add(p)
          }
        }
      }

      if (res.size >= 2 && isCyclical && res.first().isInBound == res.last().isInBound && res.first().first == res.last().last) {
        res[0] = res.first().combineWith(res.last())
        res.removeLast()
      }

      return res.filter { it.size >= 1 }
    }
  }
}

class ContinuousMaskedShape(allPoints: List<Point>, val bound: BoundRect) {
  val points: List<ContinuousPoints> = bound.splitInBounds(allPoints)
  val first = lazy { points.firstOrNull()?.first }
  val last = lazy { points.lastOrNull()?.last }

  val isCyclical get() = first == last

  fun toBoundPoints(boundInside: Boolean): List<List<Point>> =
    points.mapWithSurroundingCyclical { prev, curr, next, index ->
      if (curr.isInBound != boundInside) return@mapWithSurroundingCyclical null

      val isAtFirstEdge = index == 0 && !isCyclical
      val isAtLastEdge = index == points.size - 1 && !isCyclical

      var points: List<Point> = curr.points
      if (!isAtFirstEdge && prev.isInBound != curr.isInBound) {
        points = bound.getBoundSegment(Segment(prev.last, curr.first))?.p1?.plus(points) ?: points
      }

      if (!isAtLastEdge && next.isInBound != curr.isInBound) {
        points = points.plusIf(bound.getBoundSegment(Segment(curr.last, next.first))?.p2)
      }

      points
    }.filterNotNull()
}