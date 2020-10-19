package util

import controlP5.Button

fun <T> Iterable<T>.pEach() = println(map { it.toString() })

fun <T> List<T>.forEachWithSurrounding(block: (T?, T, T?) -> Unit) = forEachIndexed { index, item ->
  val prevItem = if (index == 0) null else this[index - 1]
  val nextItem = if (index == size - 1) null else this[index + 1]
  block(prevItem, item, nextItem)
}

fun <T> List<T>.forEachWithSurroundingCyclical(block: (T, T, T) -> Unit) = forEachIndexed { index, item ->
  val prevItem = if (index == 0) this[size - 1] else this[index - 1]
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(prevItem, item, nextItem)
}

fun <T, R> List<T>.mapWithSurrounding(block: (T?, T, T?) -> R) = mapIndexed { index, item ->
  val prevItem = if (index == 0) null else this[index - 1]
  val nextItem = if (index == size - 1) null else this[index + 1]
  block(prevItem, item, nextItem)
}

fun <T, R> List<T>.mapWithSurroundingCyclical(block: (T, T, T) -> R) = mapIndexed { index, item ->
  val prevItem = if (index == 0) this[size - 1] else this[index - 1]
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(prevItem, item, nextItem)
}

fun <T> List<T>.zipWithSiblingsCyclical() = mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }