package util.iterators

inline fun <T, reified R> Array<T>.mapArray(block: (T) -> R): Array<R> =
  Array(size) { i -> block(this[i]) }

inline fun <reified R> IntRange.mapArray(block: (Int) -> R): Array<R> =
  map { block(it) }.toTypedArray()

inline fun <T, reified R> Array<T>.mapArrayIndexed(block: (Int, T) -> R): Array<R> =
  Array(size) { i -> block(i, this[i]) }

inline fun <T, reified R> List<T>.mapArray(block: (T) -> R): Array<R> =
  Array(size) { block(this[it]) }


inline fun DoubleArray.mapDoubleArray(block: (Double) -> Double): DoubleArray =
  DoubleArray(size) { block(this[it]) }

inline fun <reified R> DoubleArray.mapArray(block: (Double) -> R): Array<R> =
  Array(size) { block(this[it]) }

inline fun <reified R> DoubleArray.mapArrayIndexed(block: (Int, Double) -> R): Array<R> =
  Array(size) { block(it, this[it]) }
