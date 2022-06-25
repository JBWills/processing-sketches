package util.polylines.iterators

import util.iterators.PolyLineIterator
import util.polylines.PolyLine

fun PolyLine.chunkedByDistance(step: Number): List<PolyLine> {
  val iterator = PolyLineIterator(this)

  val result: MutableList<PolyLine> = mutableListOf()

  while (!iterator.atEnd()) {
    result.add(iterator.move(step.toDouble()).points)
  }

  return result
}
