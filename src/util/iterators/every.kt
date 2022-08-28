package util.iterators

inline fun <T> Array<T>.everyIndexed(predicate: (Int, T) -> Boolean): Boolean {
  forEachIndexed { index, item ->
    if (!predicate(index, item)) {
      return false
    }
  }

  return true
}

inline fun <T> Iterable<T>.everyIndexed(predicate: (Int, T) -> Boolean): Boolean {
  forEachIndexed { index, item ->
    if (!predicate(index, item)) {
      return false
    }
  }

  return true
}

inline fun <T> Array<T>.every(predicate: (T) -> Boolean): Boolean =
  everyIndexed { _, item -> predicate(item) }

inline fun <T> Iterable<T>.every(predicate: (T) -> Boolean): Boolean =
  everyIndexed { _, item -> predicate(item) }
