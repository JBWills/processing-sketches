package util.polylines.walkers

import coordinate.Point
import util.polylines.PolyLine

fun pointWalk(start: Point, block: (lastPoint: Point) -> Point?): PolyLine {
  val result = mutableListOf(start)
  var next = block(start)
  while (next != null) {
    result.add(next)
    next = block(next)
  }

  return result
}
