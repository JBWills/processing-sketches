package util.iterators

fun <K, V> Map<K, V>.flipMap(): Map<V, K> = keys.associateBy { this[it]!! }
