package util.iterators

inline fun <T> List<List<T>>.deepFilter(f: (T) -> Boolean): List<List<T>> =
  map { it.filter(f) }
