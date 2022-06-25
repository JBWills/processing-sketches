package util.iterators

fun <T, R : Comparable<R>> MutableList<T>.filterInPlace(
  by: (T) -> Boolean
) {
  var currIndex = 0

  while (currIndex < size) {
    if (by(get(currIndex))) {
      currIndex += 1
    } else {
      removeAt(currIndex)
    }
  }
}
