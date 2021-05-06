package util.iterators

import coordinate.Point
import geomerativefork.src.util.bound
import util.atAmountAlong
import util.ceilInt
import util.floorInt
import util.map
import kotlin.math.max

fun <T> List<T>.endPointPair() = Pair(first(), last())

fun <T, R> Pair<List<T>, List<T>>.zip(block: (T, T) -> R): List<R> {
  if (first.size != second.size) throw Exception("Can't call zip with lists of different sizes!")

  return first.mapIndexed { index, firstItem ->
    block(firstItem, second[index])
  }
}

fun <T, R> Pair<List<T>, List<T>>.zipNullPadded(block: (T?, T?) -> R): List<R> {
  val size = max(first.size, second.size)
  return size.map { index ->
    block(first.getOrNull(index), second.getOrNull(index))
  }
}

fun <T> List<T>.mapPercentToIndex(lerpAmt: Double): Double {
  val boundAmt = lerpAmt.bound(0.0, 1.0)
  return indices.atAmountAlong(boundAmt)
}

fun <T> List<T>.getLerpIndices(lerpAmt: Double): List<Int> {
  if (isEmpty()) return listOf()

  val mappedToIndex = mapPercentToIndex(lerpAmt)
  val lowerIndex = mappedToIndex.floorInt()
  val upperIndex = mappedToIndex.ceilInt()

  return listOf(
    lowerIndex,
    upperIndex,
  )
}

fun <K, V> MutableMap<K, V>.replaceKey(oldKey: K, newKey: K) {
  val value = get(oldKey) ?: return
  put(newKey, value)
}

fun Iterable<Point>.sumPointsIndexed(block: (Int, Point) -> Point): Point {
  var sum = Point.Zero
  forEachIndexed { index, item -> sum += block(index, item) }
  return sum
}

fun <T> Iterable<T>.sumByDoubleIndexed(block: (Int, T) -> Double): Double {
  var sum = 0.0
  forEachIndexed { index, item -> sum += block(index, item) }
  return sum
}

fun <T> Iterable<T>.sumByIndexed(block: (Int, T) -> Int): Int {
  var sum = 0
  forEachIndexed { index, item -> sum += block(index, item) }
  return sum
}

/**
 * Modified from https://stackoverflow.com/a/62597698
 */
fun <T> List<T>.forEachPair(block: (index1: Int, index2: Int, item1: T, item2: T) -> Unit) {
  forEachIndexed { index1, item1 ->
    for (index2 in index1 + 1 until size) {
      block(index1, index2, item1, get(index2))
    }
  }
}

/**
 * Modified from https://stackoverflow.com/a/62597698
 */
fun <T, R> List<T>.mapEachPairNonNull(block: (index1: Int, index2: Int, item1: T, item2: T) -> R?): List<R> {
  val res = mutableListOf<R>()
  forEachIndexed { index1, item1 ->
    for (index2 in index1 + 1 until size) {
      block(index1, index2, item1, get(index2))?.let { res.add(it) }
    }
  }

  return res
}

fun <T> List<List<T>>.forEach2D(block: (rowIndex: Int, colIndex: Int, item: T) -> Unit) =
  forEachIndexed { rowIndex, row ->
    row.forEachIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T, K> List<List<T>>.map2D(block: (rowIndex: Int, colIndex: Int, item: T) -> K): List<List<K>> =
  mapIndexed { rowIndex, row ->
    row.mapIndexed { colIndex, item ->
      block(rowIndex, colIndex, item)
    }
  }

fun <T> List<T>.extendCyclical(num: Int): List<T> {
  if (isEmpty()) throw Exception("Can't call extendCyclical on empty list.")

  return List(num) { this[it % size] }
}

fun <T, K> List<List<T>>.map2DIndexed(block: (T, Int, Int) -> K) = mapIndexed { xIndex, list ->
  list.mapIndexed { yIndex, elem ->
    block(elem, xIndex, yIndex)
  }
}

fun <T> times(iterations: Int, block: (i: Int) -> T): List<T> = (0 until iterations).map(block)

fun <T> List<T>.limit(i: Int) = filterIndexed { index, _ -> index < i }

fun <T> List<T?>.filterNotNull(): List<T> = mapNotNull { it }

fun <T> Iterable<T>.pEach() = println(map { it.toString() })

fun <T> List<T>.forEachWithSurrounding(block: (T?, T, T?, Int) -> Unit) =
  forEachIndexed { index, item ->
    val prevItem = if (index == 0) null else this[index - 1]
    val nextItem = if (index == size - 1) null else this[index + 1]
    block(prevItem, item, nextItem, index)
  }

fun <T> List<T>.forEachWithSurrounding(block: (T?, T, T?) -> Unit) =
  forEachWithSurrounding { prev, curr, next, _ -> block(prev, curr, next) }

fun <T> List<T>.forEachWithNext(block: (T, T?) -> Unit) =
  forEachWithSurrounding { _, curr, next, _ -> block(curr, next) }

fun <T> List<T>.forEachWithNextIndexed(block: (T, T?, Int) -> Unit) =
  forEachWithSurrounding { _, curr, next, index -> block(curr, next, index) }

fun <T> List<T>.forEachWithSurroundingCyclical(block: (T, T, T, Int) -> Unit) =
  forEachIndexed { index, item ->
    val prevItem = if (index == 0) this[size - 1] else this[index - 1]
    val nextItem = if (index == size - 1) this[0] else this[index + 1]
    block(prevItem, item, nextItem, index)
  }

fun <T> List<T>.forEachWithSurroundingCyclical(block: (T, T, T) -> Unit) =
  forEachWithSurroundingCyclical { prev, curr, next, _ -> block(prev, curr, next) }

fun <T> List<T>.copy() = map { it }

fun <T, R> List<T>.mapIf(
  predicate: (T) -> Boolean = { true },
  block: (T) -> R,
): List<R> = mapNotNull { if (predicate(it)) block(it) else null }

fun <T> List<T>.addIf(predicate: () -> Boolean, item: () -> T) =
  if (predicate()) this + item() else this.copy()


fun <T> List<T>.prependIf(condition: Boolean, item: () -> T): List<T> =
  if (condition) listOf(item()) + this else this

fun <T> List<T>.addIf(condition: Boolean, item: () -> T) = this.addIf({ condition }, item)
fun <T> List<T>.addNotNull(item: T?) = item?.let { this + it } ?: this.copy()

fun <T, R> List<T>.mapWithLast(block: (T, Boolean) -> R) = mapIndexed { index, item ->
  block(item, index == size - 1)
}

fun <T, R> List<T>.mapWithFirst(block: (T, Boolean) -> R) = mapIndexed { index, item ->
  block(item, index == 0)
}

fun <T, R> List<T>.mapWithSurrounding(block: (T?, T, T?, Int) -> R) = mapIndexed { index, item ->
  val prevItem = if (index == 0) null else this[index - 1]
  val nextItem = if (index == size - 1) null else this[index + 1]
  block(prevItem, item, nextItem, index)
}

fun <T, R> List<T>.mapWithSurrounding(block: (T?, T, T?) -> R) =
  mapWithSurrounding { prev, curr, next, _ ->
    block(prev, curr, next)
  }

fun <T, R> List<T>.mapWithSurroundingCyclical(block: (T, T, T, Int) -> R) =
  mapIndexed { index, item ->
    val prevItem = if (index == 0) this[size - 1] else this[index - 1]
    val nextItem = if (index == size - 1) this[0] else this[index + 1]
    block(prevItem, item, nextItem, index)
  }

fun <T, R> List<T>.mapWithSurroundingCyclical(block: (T, T, T) -> R) =
  mapWithSurroundingCyclical { prev, curr, next, _ ->
    block(prev, curr, next)
  }

fun <T, R> List<T>.mapWithNextCyclical(block: (T, T) -> R) = mapIndexed { index, item ->
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(item, nextItem)
}

fun <T, R> List<T>.mapWithNext(block: (T, T) -> R) = mapWithSurrounding { _, curr, next ->
  next?.let { block(curr, next) }
}.filterNotNull()

fun <T, R> List<T>.reduceGeneral(initial: R, block: (R, T) -> R): R {
  var r = initial
  forEach { r = block(r, it) }
  return r
}

fun <T> List<T>.zipWithSiblings() =
  mapWithSurrounding { prev, curr, next -> Triple(prev, curr, next) }

fun <T> List<T>.zipWithNext(): List<Pair<T, T>> = mapWithNext { curr, next -> Pair(curr, next) }

fun <T> List<T>.zipWithSiblingsCyclical() =
  mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }
