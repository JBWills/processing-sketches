package util.iterators

import util.atAmountAlong
import util.base.doIf
import util.numbers.bound
import util.numbers.ceilInt
import util.numbers.floorInt

fun <T> List<T>.skipFirst() = if (isEmpty()) listOf() else slice(1 until size)
fun <T> List<T>.skipLast() = if (isEmpty()) listOf() else slice(0 until size - 1)

fun <T> List<T>.endPointPair() = Pair(first(), last())

fun <T> List<T>.mapPercentToIndex(lerpAmt: Double): Double {
  val boundAmt = lerpAmt.bound(0.0, 1.0)
  return indices.atAmountAlong(boundAmt)
}

fun <T> List<T>.getLerpIndices(lerpAmt: Double): List<Int> {
  if (isEmpty()) return listOf()

  val mappedToIndex = mapPercentToIndex(lerpAmt)
  val lowerIndex = mappedToIndex.floorInt()
  val upperIndex = mappedToIndex.ceilInt()

  return listOf(lowerIndex, upperIndex)
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

fun <T> List<T>.limit(i: Int) = filterIndexed { index, _ -> index < i }

fun <T> List<T?>.filterNotNull(): List<T> = mapNotNull { it }

fun <T, Attribute> Iterable<T>.groupToList(getAttr: T.() -> Attribute): List<List<T>> {
  val res = mutableMapOf<Attribute, MutableList<T>>()
  forEach {
    res.getOrPut(it.getAttr()) { mutableListOf() }.add(it)
  }

  return res.values.toList()
}

fun <T, SortingAttr : Comparable<SortingAttr>> Iterable<T>.groupToSortedList(
  sortDescending: Boolean = false,
  getGroupingAttr: T.() -> SortingAttr,
): List<List<T>> {
  val res = sortedMapOf<SortingAttr, MutableList<T>>()
  forEach {
    res.getOrPut(it.getGroupingAttr()) { mutableListOf() }.add(it)
  }

  return res.values.toList().doIf(sortDescending) { it.reversed() }
}

fun <T> List<T>.secondToLast(): T = get(size - 2)
fun <T> List<T>.secondToLastOrNull(): T? = getOrNull(size - 2)
fun <T> List<T>.secondOrNull(): T? = getOrNull(1)
