package coordinate

import coordinate.ContinuousPoints.Companion.splitInBounds
import coordinate.Point.Companion.addIf
import interfaces.shape.Maskable
import util.iterators.forEachWithSurrounding
import util.iterators.forEachWithSurroundingCyclical

fun List<Point>.isCyclical() = size > 2 && first() == last()

data class ContinuousPoints(val isInBound: Boolean, val points: MutableList<Point>) {
  val first get() = points.first()
  val last get() = points.last()
  val size get() = points.size

  fun combineWith(o: ContinuousPoints) =
    ContinuousPoints(isInBound, (points + o.points).toMutableList())

  companion object {
    fun Maskable.splitInBounds(points: List<Point>): List<ContinuousPoints> {
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

class ContinuousMaskedShape(allPoints: List<Point>, val bound: Maskable) {
  val points: List<ContinuousPoints> = bound.splitInBounds(allPoints)
  val first = lazy { points.firstOrNull()?.first }
  val last = lazy { points.lastOrNull()?.last }

  private val isCyclical get() = first == last

  fun toBoundPoints(boundInside: Boolean): List<List<Point>> {
    val result: MutableList<List<Point>> = mutableListOf()

    points.forEachWithSurroundingCyclical { prev, curr, next, index ->
      val isAtFirstEdge = index == 0 && !isCyclical
      val isAtLastEdge = index == points.size - 1 && !isCyclical

      val prevBoundSegments = bound.intersection(Segment(prev.last, curr.first))
      val nextBoundSegments = bound.intersection(Segment(curr.last, next.first))

      if (!isAtFirstEdge && prevBoundSegments.isNotEmpty() && curr.isInBound != boundInside && prev.isInBound != boundInside) {
        result.addAll(prevBoundSegments.map { it.asPolyLine })
      }

      if (!isAtLastEdge && nextBoundSegments.isNotEmpty() && curr.isInBound != boundInside && next.isInBound != boundInside) {
        result.addAll(nextBoundSegments.map { it.asPolyLine })
      }

      if (curr.isInBound == boundInside) {
        val pointsSoFar = mutableListOf<Point>()
        if (!isAtFirstEdge && prev.isInBound != curr.isInBound) {
          pointsSoFar.addIf(prevBoundSegments.firstOrNull()?.p1)
        }

        pointsSoFar.addAll(curr.points)

        if (!isAtLastEdge && next.isInBound != curr.isInBound) {
          pointsSoFar.addIf(nextBoundSegments.lastOrNull()?.p2)
        }

        result.add(pointsSoFar)
      } else {
        // consider adding logic here to segmentize all outside points, since they could cross over
        // the bound box
      }

    }

    return result
  }
}
