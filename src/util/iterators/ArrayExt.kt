package util.iterators

inline fun <T, reified R> Array<T>.mapArrayIndexed(block: (Int, T) -> R): Array<R> =
  Array(size) { i -> block(i, this[i]) }

inline fun <T, reified R> List<T>.mapArray(block: (T) -> R): Array<R> =
  Array(this.size) { block(this[it]) }

inline fun <reified R> DoubleArray.mapArray(block: (Double) -> R): Array<R> =
  Array(this.size) { block(this[it]) }

inline fun <reified T> timesArray(iterations: Int, block: (i: Int) -> T): Array<T> =
  (0 until iterations).map(block).toTypedArray()

inline fun <reified T> Array<Array<T>>.flattenArray(): Array<T> =
  flatMap { it.toList() }.toTypedArray()

inline fun <reified T> varargIf(conditional: Boolean, item: T): Array<T> =
  if (conditional) arrayOf(item) else arrayOf()

inline fun <reified T> ifOrNull(conditional: Boolean, item: T): T? =
  if (conditional) item else null

inline fun <reified T> Array<T?>.filterNotNull(): Array<T> = mapNotNull { it }.toTypedArray()
inline fun <T, reified K> Array<T>.mapArrayNotNull(block: (i: T) -> K?): Array<K> =
  mapNotNull { block(it) }.toTypedArray()

inline fun IntArray.mapIntArray(block: (index: Int, value: Int) -> Int): IntArray =
  IntArray(size) { block(it, get(it)) }

