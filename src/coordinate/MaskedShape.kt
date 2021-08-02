package coordinate

import coordinate.ContinuousPoints.Companion.splitInBounds
import coordinate.Point.Companion.addIf
import interfaces.shape.Maskable
import util.iterators.forEachWithSurroundingCyclical
import util.polylines.MutablePolyLine
import util.polylines.connectWith
import util.polylines.polyLine.PolyLine
import util.polylines.polyLine.isClosed

data class ContinuousPoints(val isInBound: Boolean, val points: MutablePolyLine) :
  MutablePolyLine by points {

  fun combineWith(o: ContinuousPoints) =
    ContinuousPoints(isInBound, points.connectWith(o.points).toMutableList())

  companion object {
    fun Maskable.splitInBounds(points: PolyLine): List<ContinuousPoints> {
      if (points.size < 2) return listOf()

      val res = mutableListOf<ContinuousPoints>()

      points.forEach { p ->
        val currInBound = contains(p)
        if (res.isEmpty() || res.last().isInBound != currInBound) {
          res.add(ContinuousPoints(currInBound, mutableListOf(p)))
        } else {
          res.last().points.add(p)
        }
      }

      if (res.size >= 2 &&
        points.isClosed() &&
        res.first().isInBound == res.last().isInBound &&
        res.first().first() == res.last().last()
      ) {
        res[0] = res.first().combineWith(res.last())
        res.removeLast()
      }

      return res.filter { it.size >= 1 }
    }
  }
}

class ContinuousMaskedShape(allPoints: PolyLine, val bound: Maskable) {
  val points: List<ContinuousPoints> = bound.splitInBounds(allPoints)
  val first = lazy { points.firstOrNull()?.first() }
  val last = lazy { points.lastOrNull()?.last() }

  private val isClosed get() = first == last

  fun toBoundPoints(boundInside: Boolean): List<PolyLine> {
    val result: MutableList<PolyLine> = mutableListOf()

    points.forEachWithSurroundingCyclical { prev, curr, next, index ->
      val isAtFirstEdge = index == 0 && !isClosed
      val isAtLastEdge = index == points.size - 1 && !isClosed

      val prevBoundSegments = bound.intersection(Segment(prev.last(), curr.first()))
      val nextBoundSegments = bound.intersection(Segment(curr.last(), next.first()))

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
