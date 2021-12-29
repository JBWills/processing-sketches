package util.comparators

class NumberComparator : Comparator<Number> {
  override fun compare(o1: Number, o2: Number): Int {
    return when {
      o1 == o2 -> 0
      o1.toDouble() < o2.toDouble() -> -1
      else -> 1
    }
  }
}
