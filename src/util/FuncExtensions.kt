package util

fun <T, V> whenNotNull(o: T?, f: (T) -> V) {
  if (o != null) f(o)
}

fun <T> T.with(block: T.() -> Unit) = block(this)