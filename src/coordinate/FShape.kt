package coordinate

import util.DoubleRange
import util.at
import util.percentAlong

open class FShape(val f: (Double, Double) -> Point, val tRange: DoubleRange) : Walkable {

  open fun pointAt(percentAlong: Double) = f(tRange.at(percentAlong), percentAlong)
  
  override fun walk(step: Double): List<Point> {
    val res = mutableListOf<Point>()
    var i = tRange.start
    while (i < tRange.endInclusive + step) {
      res.add(f(i, tRange.percentAlong(i)))
      i += step
    }

    return res
  }

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