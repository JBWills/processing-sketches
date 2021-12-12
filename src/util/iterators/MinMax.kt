@file:Suppress("unused")

package util.iterators

import util.base.DoubleRange

private fun <T> T.minMax(foreach: T.(action: (Int) -> Unit) -> Unit): IntRange? {
  var min: Int? = null
  var max: Int? = null

  foreach { item ->
    min.let { frozenMin ->
      if (frozenMin == null || item < frozenMin) {
        min = item
      }
    }

    max.let { frozenMax ->
      if (frozenMax == null || item > frozenMax) {
        max = item
      }
    }
  }

  val minNonNull = min ?: return null
  val maxNonNull = max ?: return null

  return minNonNull..maxNonNull
}

private fun <T> T.minMax(foreach: T.(action: (Double) -> Unit) -> Unit): DoubleRange? {
  var min: Double? = null
  var max: Double? = null

  foreach { item ->
    min.let { frozenMin ->
      if (frozenMin == null || item < frozenMin) {
        min = item
      }
    }

    max.let { frozenMax ->
      if (frozenMax == null || item > frozenMax) {
        max = item
      }
    }
  }

  val minNonNull = min ?: return null
  val maxNonNull = max ?: return null

  return minNonNull..maxNonNull
}


val DoubleArray.minMax: DoubleRange?
  get() = minMax(DoubleArray::forEach)


val IntArray.minMax: IntRange?
  get() = minMax(IntArray::forEach)

val List<Double>.minMax: DoubleRange?
  get() = minMax(List<Double>::forEach)

val List<Int>.minMax: IntRange?
  get() = minMax(List<Int>::forEach)

val Array<Double>.minMax: DoubleRange?
  get() = minMax(Array<Double>::forEach)

val Array<Int>.minMax: IntRange?
  get() = minMax(Array<Int>::forEach)
