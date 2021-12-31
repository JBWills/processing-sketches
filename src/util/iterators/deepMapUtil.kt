package util.iterators

inline fun <T, K> List<List<T>>.deepMap(f: (T) -> K): List<List<K>> =
  map { it.map(f) }

inline fun <T, R> List<List<List<T>>>.deepDeepMap(block: (T) -> R): List<List<List<R>>> =
  map { outerList ->
    outerList.map { list -> list.map(block) }
  }

inline fun <T, K> List<List<T>>.deepMapNotNull(f: (T) -> K?): List<List<K>> =
  map { it.mapNotNull(f) }
