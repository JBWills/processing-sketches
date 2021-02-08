package geomerativefork.src.util

import kotlin.math.max

fun <T, K> Array<K>.reduceTo(initialAcc: T, reducer: (T, K) -> T): T {
  var acc = initialAcc

  forEach { elem -> acc = reducer(acc, elem) }

  return acc
}

fun <T, K> Iterator<K>.reduceTo(initialAcc: T, reducer: (T, K) -> T): T {
  var acc = initialAcc

  forEach { elem -> acc = reducer(acc, elem) }

  return acc
}

inline fun <reified T> Array<T?>.filterArrayNotNull(): Array<T> {
  val x = mutableListOf<T>()

  forEach {
    if (it != null) x.add(it)
  }

  return x.toTypedArray()
}

inline fun <T, reified R> Array<T>.mapArrayWithNext(block: (T, T) -> R): Array<R> {

  val nullableArr: Array<R?> = arrayOfNulls(max(size - 1, 0))

  forEachIndexed { index, elem ->
    nullableArr[index]
    val hasNext = index < size - 1
    if (hasNext) {
      nullableArr[index] = block(elem, this[index + 1])
    }
  }

  return nullableArr as Array<R>
}

inline fun <T, reified R> Array<T>.mapArray(block: (T) -> R): Array<R> =
  Array(size) { i -> block(this[i]) }

inline fun <T, reified R> Array<T>.flatMapArray(block: (T) -> Array<R>): Array<R> {
  val l = mutableListOf<R>()
  forEach { l.addAll(block(it)) }

  return l.toTypedArray()
}

fun <T> Array<T>.toArrayString() = "[${joinToString(", ") { it.toString() }}]"
