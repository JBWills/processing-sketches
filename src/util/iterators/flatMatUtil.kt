package util.iterators

inline fun <T, K> List<List<T>>.flatMapNonNull(block: (T) -> K?): List<K> =
  mutableListOf<K>()
    .also { newList ->
      deepForEach { point -> newList.addNotNull(block(point)) }
    }
