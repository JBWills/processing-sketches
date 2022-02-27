package util.iterators

import util.numbers.times

inline fun <reified T> forEachSteppedIndexed(
  step: Int,
  size: Int,
  get: (Int) -> T,
  block: (Array<T>, Int) -> Unit
) {
  var i = 0

  while (i < size) {
    val list = Array<T?>(step) { null }

    step.times { stepIndex ->
      val arrIndex = stepIndex + i

      if (arrIndex < size) {
        list[stepIndex] = get(arrIndex)
      }
    }

    block(list.filterNotNull(), i / step)

    i += step
  }
}

inline fun <T> List<T>.forEachSteppedIndexed(
  step: Int,
  block: (List<T>, Int) -> Unit
) {
  var i = 0

  while (i < size) {
    val list = mutableListOf<T>()

    step.times { stepIndex ->
      val arrIndex = stepIndex + i

      if (arrIndex < size) {
        list.add(get(arrIndex))
      }
    }

    block(list, i / step)

    i += step
  }
}

private fun <T, K, R> ((T) -> R).addUnusedArg(): (T, K) -> R = { t: T, _: K -> this(t) }

fun DoubleArray.forEachSteppedIndexed(step: Int, block: (DoubleArray, Int) -> Unit) =
  forEachSteppedIndexed(step, size, { get(it) }) { arr, index -> block(arr.toDoubleArray(), index) }

fun DoubleArray.forEachStepped(step: Int, block: (DoubleArray) -> Unit) =
  forEachSteppedIndexed(step, block.addUnusedArg())

fun ByteArray.forEachSteppedIndexed(step: Int, block: (ByteArray, Int) -> Unit) =
  forEachSteppedIndexed(step, size, { get(it) }) { arr, index -> block(arr.toByteArray(), index) }

fun ByteArray.forEachStepped(step: Int, block: (ByteArray) -> Unit) =
  forEachSteppedIndexed(step, block.addUnusedArg())

fun IntArray.forEachSteppedIndexed(step: Int, block: (IntArray, Int) -> Unit) =
  forEachSteppedIndexed(step, size, { get(it) }) { arr, index -> block(arr.toIntArray(), index) }

fun IntArray.forEachStepped(step: Int, block: (IntArray) -> Unit) =
  forEachSteppedIndexed(step, block.addUnusedArg())

fun FloatArray.forEachSteppedIndexed(step: Int, block: (FloatArray, Int) -> Unit) =
  forEachSteppedIndexed(step, size, { get(it) }) { arr, index -> block(arr.toFloatArray(), index) }

fun FloatArray.forEachStepped(step: Int, block: (FloatArray) -> Unit) =
  forEachSteppedIndexed(step, block.addUnusedArg())

fun <T> List<T>.forEachStepped(step: Int, block: (List<T>) -> Unit) =
  forEachSteppedIndexed(step, block.addUnusedArg())
