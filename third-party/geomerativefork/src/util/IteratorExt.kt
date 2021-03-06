package geomerativefork.src.util

import kotlin.math.max

fun <T, R> List<List<T>>.deepMap(block: (T) -> R): List<List<R>> = map { list -> list.map(block) }

fun <A> Iterable<A>.mapIf(predicate: Boolean, block: (A) -> A): List<A> =
  if (predicate) map(block) else this.toList()

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

inline fun <T, reified R> List<T>.mapArray(block: (T) -> R): Array<R> =
  Array(size) { i -> block(this[i]) }

inline fun <reified R> IntRange.mapArray(block: (Int) -> R): Array<R> =
  map { block(it) }.toTypedArray()

inline fun <T, reified R> Iterable<T>.flatMapArray(block: (T) -> Array<R>): Array<R> =
  flatMapArrayIndexed { _, elem -> block(elem) }

inline fun <T, reified R> Iterable<T>.flatMapArrayIndexed(block: (Int, T) -> Array<R>): Array<R> {
  val l = mutableListOf<R>()
  forEachIndexed { index, elem -> l.addAll(block(index, elem)) }

  return l.toTypedArray()
}

inline fun <T, reified R> Array<T>.flatMapArray(block: (T) -> Array<R>): Array<R> =
  flatMapArrayIndexed { _, elem -> block(elem) }

inline fun <T, reified R> Array<T>.flatMapArrayIndexed(block: (Int, T) -> Array<R>): Array<R> {
  val l = mutableListOf<R>()
  forEachIndexed { index, elem -> l.addAll(block(index, elem)) }

  return l.toTypedArray()
}

fun <T> Array<T>.toArrayString() = "[${joinToString(", ") { it.toString() }}]"
