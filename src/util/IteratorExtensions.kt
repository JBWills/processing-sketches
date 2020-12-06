package util

fun <T> List<T?>.filterNotNull(): List<T> = mapNotNull { it }

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

fun <T> List<T>.copy() = map { it }

fun <T> List<T>.addIf(predicate: () -> Boolean, item: () -> T) = if (predicate()) this + item() else this.copy()
fun <T> List<T>.addIf(condition: Boolean, item: () -> T) = this.addIf({ condition }, item)
fun <T> List<T>.addNotNull(item: T?) = item?.let { this + it } ?: this.copy()

fun <T, R> List<T>.mapWithLast(block: (T, Boolean) -> R) = mapIndexed { index, item ->
  block(item, index == size - 1)
}

fun <T, R> List<T>.mapWithFirst(block: (T, Boolean) -> R) = mapIndexed { index, item ->
  block(item, index == 0)
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

fun <T, R> List<T>.mapWithNextCyclical(block: (T, T) -> R) = mapIndexed { index, item ->
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(item, nextItem)
}

fun <T, R> List<T>.mapWithNext(block: (T, T) -> R) = mapWithSurrounding { _, curr, next ->
  next?.let { block(curr, next) }
}.filterNotNull()

fun <T> List<T>.zipWithSiblings() = mapWithSurrounding { prev, curr, next -> Triple(prev, curr, next) }

fun <T> List<T>.zipWithNext(): List<Pair<T, T>> = mapWithNext { curr, next -> Pair(curr, next) }

fun <T> List<T>.zipWithSiblingsCyclical() = mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }