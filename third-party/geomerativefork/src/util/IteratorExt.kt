package geomerativefork.src.util

import kotlin.math.max


/**
 * Iterate over list, evaluating each item as in predicate or not, if it is not, split the list and
 * discard the unmatched elements.
 *
 * @param T The type of the list
 * @param predicate true if the item should be in the result
 * @return a list of lists whos summed size is no larger than the original list
 */
fun <T> Iterable<T>.chunkFilter(predicate: (T) -> Boolean): List<List<T>> {
  var lastInside: Boolean? = null

  val result = mutableListOf<MutableList<T>>()
  forEach {
    val inside = predicate(it)
    if (inside) {
      if (inside != lastInside) result.add(mutableListOf())
      result.last().add(it)
    }

    lastInside = inside
  }

  return result
}

/**
 * Iterate over list, evaluating each item as in predicate or not, if it is not, split the list and
 * discard the unmatched elements. For boundaries (ex: between indexes 1-2 in
 * [inPred, inPred, outOfPred, outOfPred]) this function will ask for the true boundary value and will
 * append that to the end of the result list.
 *
 * @param T The type of the list
 * @param predicate true if the item should be in the result
 * @param getBoundaryValue function that takes two values that represent the current and next items
 *  in the list where a boundary occurs. Returns a new value that represents the boundary value that
 *  should be added to the inPred list. If this returns null, no value is added to the list
 * @return a list of lists whos summed size is no larger than the original list
 */
fun <T> List<T>.chunkFilterInterpolated(
  predicate: (T) -> Boolean,
  getBoundaryValue: (Triple<T, Int, Boolean>, Triple<T, Int, Boolean>) -> T?
): List<List<T>> {
  var lastValue: Pair<T, Boolean>? = null

  val result = mutableListOf<MutableList<T>>()

  forEachIndexed { index, item ->
    val inside = predicate(item)
    if (inside != lastValue?.second) {
      result.add(mutableListOf())

      lastValue?.let { (lastItem, lastInside) ->
        getBoundaryValue(
          Triple(lastItem, index - 1, lastInside),
          Triple(item, index, inside),
        )?.let { boundaryItem ->
          val listToAddBoundaryItem = if (inside) result.last() else result[result.size - 2]
          listToAddBoundaryItem.add(boundaryItem)
        }
      }
    }

    if (inside) result.last().add(item)

    lastValue = item to inside
  }

  return result
}

fun <T, R> List<List<T>>.deepMap(block: (T) -> R): List<List<R>> = map { list -> list.map(block) }
fun <T, R> List<List<List<T>>>.deepDeepMap(block: (T) -> R): List<List<List<R>>> =
  map { outerList ->
    outerList.map { list -> list.map(block) }
  }

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
