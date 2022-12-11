package util.iterators

inline fun <T> List<List<T>>.deepForEach(f: (T) -> Unit): Unit =
  forEach { it.forEach(f) }

