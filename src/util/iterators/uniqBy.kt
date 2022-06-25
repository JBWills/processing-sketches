package util.iterators

fun <T, R : Comparable<R>> MutableList<T>.uniqByInPlace(
  by: (T) -> R?
) = uniqByInPlace(true, by)

fun <T, R : Comparable<R>> MutableList<T>.uniqByInPlace(
  keepFirst: Boolean,
  by: (T) -> R?
) {
  val set = mutableSetOf<R?>()
  var currIndex = if (keepFirst) 0 else size - 1

  while ((keepFirst && currIndex < size) || (!keepFirst && currIndex >= 0)) {
    val item = get(currIndex)
    val added = set.add(by(item))
    if (added) {
      currIndex += if (keepFirst) 1 else -1
    } else {
      // element already contained in set
      removeAt(currIndex)
      if (!keepFirst) {
        currIndex -= 1
      }
    }
  }
}
