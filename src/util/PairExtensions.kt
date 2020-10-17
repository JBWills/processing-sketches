package util

fun <T, K> Pair<T, T>.map(f: (T) -> K): Pair<K, K> = Pair(f(first), f(second))
fun <T, K> Pair<T, K>.reversed(): Pair<K, T> = Pair(second, first)