package util.iterators

fun checkValidN(size: Int, n: Int) {
  if (size % n != 0) {
    throw Exception("Error in mapEvery: array length must be divisible by n. Array length: $size, n: $n")
  } else if (n == 0) {
    throw Exception("Error in mapEvery: n cannot be 0.")
  }
}

inline fun <reified T, reified R> Array<T>.mapEvery(n: Int, block: (Array<T>) -> R): Array<R> {
  checkValidN(size, n)

  return Array(size / n) { i ->
    val start = i * n
    val end = start + n

    block(sliceArray(start until end))
  }
}

fun <T, R> List<T>.mapEvery(n: Int, block: (List<T>) -> R): List<R> {
  checkValidN(size, n)
  return chunked(n).map(block)
}

inline fun <reified R> IntArray.mapEvery(n: Int, block: (Array<Int>) -> R): Array<R> =
  toTypedArray().mapEvery(n, block)

inline fun <reified R> DoubleArray.mapEvery(
  n: Int,
  block: (Array<Double>) -> R
): Array<R> =
  toTypedArray().mapEvery(n, block)

inline fun <reified R> FloatArray.mapEvery(
  n: Int,
  block: (Array<Float>) -> R
): Array<R> =
  toTypedArray().mapEvery(n, block)

