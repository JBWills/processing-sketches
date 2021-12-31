package util.iterators

inline fun <T> List<List<T>>.deepForEach(f: (T) -> Unit): Unit =
  forEach { it.forEach(f) }

inline fun <T> List<List<List<T>>>.deepDeepForEach(block: (T) -> Unit): Unit =
  forEach { outerList ->
    outerList.forEach { list -> list.forEach(block) }
  }
