package util.algorithms

import coordinate.Point
import coordinate.Segment
import util.numbers.times
import util.polylines.PolyLine
import util.polylines.mapBySegment
import util.tuple.map
import java.util.LinkedList
import java.util.Queue


fun PolyLine.douglassPeucker(epsilon: Double): PolyLine {
  // Find the point with the maximum distance
  var dmax = 0.0
  var index = 0
  if (size < 2) return this

  val startToEnd = Segment(first(), last())

  forEachIndexed { i, point ->
    if (i == 0 || i == size - 1) return@forEachIndexed
    val d = point.perpendicularDistanceTo(startToEnd)
    if (d > dmax) {
      index = i
      dmax = d
    }
  }

  // If max distance is greater than epsilon, recursively simplify
  return if (dmax > epsilon) {
    (0 until index to (index until size)).map {
      slice(it).douglassPeucker(epsilon)
    }.toList().flatten()
  } else {
    listOf(first(), last())
  }
}

/**
 * From https://keithmaggio.wordpress.com/2018/05/29/math-magician-path-smoothing-with-chaikin/
 *
 * @param path
 * @return
 */
fun PolyLine.chaikin(): PolyLine {
  if (this.isEmpty()) return listOf()
  val ret = mutableListOf(first())
  val procPath: Queue<Point> = LinkedList(this)
  var prevPoint: Point = procPath.remove()
  while (procPath.size > 1) {
    val curPoint = procPath.remove()
    val nextPoint = procPath.peek()
    val currentHeading = (curPoint - prevPoint).normalized
    val nextHeading = (nextPoint - curPoint).normalized
    val angle = Segment(currentHeading, nextHeading).slope.value
    if (angle >= 30) {
      if (angle >= 90) {
        procPath.remove()
        prevPoint = curPoint
        continue
      }
      val pointQ = curPoint * 0.75 + nextPoint * 0.25
      val pointR = curPoint * 0.25 + nextPoint * 0.75
      ret.add(pointQ)
      ret.add(pointR)
      prevPoint = pointR
    } else {
      ret.add(curPoint)
      prevPoint = curPoint
    }
  }

  // Make sure we get home.
  if (!ret.contains(last())) ret.add(last())
  return ret
}

fun PolyLine.chaikin2(): PolyLine {
  if (size < 2) return this
  val chaikinPoints = mapBySegment { segment ->
    listOf(segment.getPointAtPercent(0.25), segment.getPointAtPercent(0.75))
  }.flatten()

  return first() + chaikinPoints + last()
}

fun PolyLine.chaikin(times: Int): PolyLine {
  var res = this

  times.times {
    res = res.chaikin2()
  }

  return res
}
