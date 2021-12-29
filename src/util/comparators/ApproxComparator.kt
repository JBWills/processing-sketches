package util.comparators

import kotlin.math.abs

class ApproxComparator(approxAmount: Number) : Comparator<Number> {
  private val approxAmountDouble = approxAmount.toDouble()

  override fun compare(o1: Number, o2: Number): Int {
    val o1d = o1.toDouble()
    val o2d = o2.toDouble()
    if (abs(o1d - o2d) < approxAmountDouble) return 0
    if (o1d < o2d) return -1
    return 1
  }
}
