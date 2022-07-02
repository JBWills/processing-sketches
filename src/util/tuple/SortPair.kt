package util.tuple

fun <T : Comparable<T>> Pair<T, T>.sort(): Pair<T, T> {
  if (first > second) {
    return second to first
  }

  return this
}
