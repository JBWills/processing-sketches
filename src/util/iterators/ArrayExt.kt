package util.iterators

inline fun <T, reified R> List<T>.mapArray(block: (T) -> R): Array<R> =
  Array(this.size) { block(this[it]) }

inline fun <reified T> timesArray(iterations: Int, block: (i: Int) -> T): Array<T> = (0 until iterations).map(block).toTypedArray()

inline fun <reified T> Array<Array<T>>.flattenArray(): Array<T> = flatMap { it.toList() }.toTypedArray()
