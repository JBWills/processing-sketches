package util.image.algorithms

import coordinate.Point
import org.opencv.core.Mat
import util.image.opencvMat.get
import util.iterators.chunkFilter
import util.numbers.greaterThanEqualToDelta
import util.polylines.PolyLine
import util.polylines.walk

private const val DefaultDitherStep = 5.0

private data class PointAndValue(val point: Point, val value: Double?)

private fun Mat.getPointAndValue(p: Point) = PointAndValue(p, get(p))

fun Mat.ditherAlongPath(path: PolyLine, step: Double = DefaultDitherStep): List<PolyLine> {
  return path.walk(step).chunkFilter { (_, curr, _, _) ->
    get(curr)?.greaterThanEqualToDelta(0.5) ?: false
  }
}
