package util.iterators

import coordinate.Point
import util.map
import kotlin.math.max

fun <T> Sequence<T>.iterate() = forEach { _ -> }

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

fun <K, V> MutableMap<K, V>.replaceKey(oldKey: K, newKey: K) {
  val value = get(oldKey) ?: return
  if (oldKey == newKey) return
  put(newKey, value)
  remove(oldKey)
}

fun Iterable<Point>.sumPointsIndexed(block: (Int, Point) -> Point): Point =
  mapIndexed(block)
    .reduceGeneral(Point.Zero) { acc, p -> acc + p }

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

fun <T> times(iterations: Int, block: (i: Int) -> T): List<T> = (0 until iterations).map(block)

@Suppress("unused")
fun <T> Iterable<T>.pEach() = println(map { it.toString() })


fun <T> Iterable<T>.copy(): List<T> = map { it }

fun <T, R> Iterable<T>.mapIf(
  predicate: (T) -> Boolean = { true },
  block: (T) -> R,
): List<R> = mapNotNull { if (predicate(it)) block(it) else null }

fun <T> Iterable<T>.addIf(predicate: () -> Boolean, item: () -> T) =
  if (predicate()) this + item() else this.copy()


fun <T> Iterable<T>.prependIf(condition: Boolean, getItem: () -> T): List<T> =
  if (condition) listOf(getItem()) + this else toList()

fun <T> Iterable<T>.prependIf(condition: Boolean, item: T): List<T> =
  prependIf(condition) { item }

fun <T> Iterable<T>.addIf(condition: Boolean, item: () -> T) = this.addIf({ condition }, item)
fun <T> Iterable<T>.addIf(condition: Boolean, item: T): List<T> =
  addIf(condition) { item }

fun <T> Iterable<T>.addNotNull(item: T?) = item?.let { this + it } ?: this.copy()


fun <T, R> Iterable<T>.reduceGeneral(initial: R, block: (R, T) -> R): R {
  var r = initial
  forEach { r = block(r, it) }
  return r
}
