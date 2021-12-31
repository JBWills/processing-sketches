package util.iterators

fun <T> MutableList<T>.addNotNull(item: T?) {
  item?.let { add(it) }
}
