package util.comparators

open class OrderedComparator<T>(
  private val getFieldList: List<((T) -> Number)>,
  private val customComparator: Comparator<Number> = NumberComparator()
) : Comparator<T> {
  override fun compare(o1: T, o2: T): Int {
    if (o1 == o2) return 0

    getFieldList.forEach { getField ->
      val res = customComparator.compare(getField(o1), getField(o2))

      if (res != 0) {
        return@compare res
      }
    }

    return 0
  }
}
