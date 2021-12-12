package util.iterators

private inline fun <T, R> mapOrNull(item: T, predicate: (T) -> Boolean, map: (T) -> R): R? =
  if (predicate(item)) map(item)
  else null

fun <T, R> Iterable<T>.filterMap(predicate: (T) -> Boolean, map: (T) -> R): List<R> = mapNotNull {
  mapOrNull(it, predicate, map)
}

fun <T, R> Array<T>.filterMap(predicate: (T) -> Boolean, map: (T) -> R): List<R> = mapNotNull {
  mapOrNull(it, predicate, map)
}

fun <R> DoubleArray.filterMap(predicate: (Double) -> Boolean, map: (Double) -> R): List<R> {
  val result = mutableListOf<R>()

  forEach {
    mapOrNull(it, predicate, map)?.let { mappedItem -> result.add(mappedItem) }
  }

  return result
}

fun <R> FloatArray.filterMap(predicate: (Float) -> Boolean, map: (Float) -> R): List<R> {
  val result = mutableListOf<R>()

  forEach {
    mapOrNull(it, predicate, map)?.let { mappedItem -> result.add(mappedItem) }
  }

  return result
}

fun <R> IntArray.filterMap(predicate: (Int) -> Boolean, map: (Int) -> R): List<R> {
  val result = mutableListOf<R>()

  forEach {
    mapOrNull(it, predicate, map)?.let { mappedItem -> result.add(mappedItem) }
  }

  return result
}
