package util.iterators

import coordinate.Point

fun Array<DoubleArray>.contains(p: Point): Boolean =
  p.xi in 0..size && p.yi in 0..(firstOrNull()?.size ?: -1)

fun Array<DoubleArray>.getOr(p: Point, getDefault: () -> Double): Double =
  if (contains(p)) this[p.xi][p.yi] else getDefault()

fun DoubleArray.setOrNull(index: Int, d: Double): Boolean = if (index in indices) {
  this[index] += d
  true
} else false

fun Array<DoubleArray>.getOrNull(outerIndex: Int, innerIndex: Int): Double? =
  if (outerIndex in indices && innerIndex in this[outerIndex].indices) {
    this[outerIndex][innerIndex]
  } else null

fun Array<DoubleArray>.setOrNull(outerIndex: Int, innerIndex: Int, d: Double): Boolean =
  if (outerIndex in indices && innerIndex in this[outerIndex].indices) {
    this[outerIndex][innerIndex] = d
    true
  } else false

fun Array<DoubleArray>.addOrNull(outerIndex: Int, innerIndex: Int, d: Double): Boolean =
  if (outerIndex in indices && innerIndex in this[outerIndex].indices) {
    this[outerIndex][innerIndex] += d
    true
  } else false
