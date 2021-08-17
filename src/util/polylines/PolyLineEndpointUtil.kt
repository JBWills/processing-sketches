package util.polylines

import coordinate.Point
import util.iterators.copy

typealias EndpointUpdater = (Pair<Point, Point>) -> Pair<Point, Point>

fun PolyLine.moveEndpoints(getNewEndpoints: EndpointUpdater): PolyLine {
  if (size == 0) return listOf()
  else if (size == 1) return copy()
  val (newFirst, newLast) = getNewEndpoints(first() to last())

  return newFirst + slice(1..(size - 2)) + newLast
}

@JvmName("moveEndpointsList")
fun List<PolyLine>.moveEndpoints(getNewEndpoints: EndpointUpdater): List<PolyLine> =
  map { it.moveEndpoints(getNewEndpoints) }
