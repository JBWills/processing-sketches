package util.iterators

import util.tuple.toEntry
import java.util.Hashtable

fun <T, Attribute> Iterable<T>.groupValuesBy(getAttr: T.() -> Attribute): Map<Attribute, List<T>> {
  val res = mutableMapOf<Attribute, MutableList<T>>()
  forEach {
    res.getOrPut(it.getAttr()) { mutableListOf() }.add(it)
  }

  return res
}

fun <K, V, R> Map<K, Iterable<V>>.deepMapValues(block: (Map.Entry<K, V>) -> R): Map<K, List<R>> =
  mapValues { (k, v) -> v.map { block((k to it).toEntry()) } }


fun <K, V> Map<K, V>.toHashTable(): Hashtable<K, V> = Hashtable(this)

fun <K, V> MutableMap<K, V>.putIf(
  condition: Boolean,
  getEntry: () -> Pair<K, V>
): MutableMap<K, V> {
  if (condition) {
    val (k, v) = getEntry()
    this[k] = v
  }

  return this
}

fun <K, V> mapOf(
  vararg pairs: Pair<K, V>?
): Map<K, V> = hashMapOf(*pairs.filterNotNull().toTypedArray())
