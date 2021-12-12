package util.iterators

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

@JvmName("mapArrayOfInts")
inline fun <reified T> IntArray.mapArray(block: (index: Int, value: Int) -> T): Array<T> =
  Array(size) { block(it, get(it)) }

// from https://gist.github.com/yuhki50/ca8363c0bee588cd79ad6637edfaedf3
fun IntArray.toByteArray(): ByteArray =
  foldIndexed(ByteArray(size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
