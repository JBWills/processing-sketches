package util.tuple

fun <T, K> Pair<T, T>.map(f: (T) -> K): Pair<K, K> = Pair(f(first), f(second))
fun <T, K> Pair<List<T>, List<T>>.mapPairOfLists(f: (T) -> K): Pair<List<K>, List<K>> =
  Pair(first.map(f), second.map(f))

fun <T, K> Pair<T, K>.reversed(): Pair<K, T> = Pair(second, first)

infix fun <T, K> T.and(other: K): Pair<T, K> = this to other

// Code from https://discuss.kotlinlang.org/t/pair-should-implement-map-entry/11917
fun <K, V> Pair<K, V>.toEntry() = object : Map.Entry<K, V> {
  override val key: K = first
  override val value: V = second
}
