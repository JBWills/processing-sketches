package coordinate

import interfaces.shape.Walkable
import util.atAmountAlong
import util.base.DoubleRange
import util.percentAlong

open class FShape(val f: (Double, Double) -> Point, val tRange: DoubleRange) : Walkable {

  open fun pointAt(percentAlong: Double) = f(tRange.atAmountAlong(percentAlong), percentAlong)

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> {
    val res = mutableListOf<T>()
    var i = tRange.start
    while (i < tRange.endInclusive + step) {
      res.add(block(f(i, tRange.percentAlong(i))))
      i += step
    }

    return res
  }
}
