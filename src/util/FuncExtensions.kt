package util

fun <T, V> whenNotNull(o: T?, f: (T) -> V) {
  if (o != null) f(o)
}