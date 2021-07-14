package util.iterators

fun <T, Attribute> Iterable<T>.groupValuesBy(getAttr: T.() -> Attribute): Map<Attribute, List<T>> {
  val res = mutableMapOf<Attribute, MutableList<T>>()
  forEach {
    res.getOrPut(it.getAttr()) { mutableListOf() }.add(it)
  }

  return res
}

fun <K, V, R> Map<K, Iterable<V>>.flatMapValues(block: (Pair<K, V>) -> R): Map<K, List<R>> =
  mapValues { (k, v) -> v.map { block(k to it) } }
