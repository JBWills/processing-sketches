package util

fun <T> times(iterations: Int, block: (i: Int) -> T): List<T> = (0 until iterations).map(block)

fun <T> List<T>.limit(i: Int) = filterIndexed { index, item -> index < i }

fun <T> List<T?>.filterNotNull(): List<T> = mapNotNull { it }

fun <T> Iterable<T>.pEach() = println(map { it.toString() })

fun <T> List<T>.forEachWithSurrounding(block: (T?, T, T?, Int) -> Unit) = forEachIndexed { index, item ->
  val prevItem = if (index == 0) null else this[index - 1]
  val nextItem = if (index == size - 1) null else this[index + 1]
  block(prevItem, item, nextItem, index)
}

fun <T> List<T>.forEachWithSurrounding(block: (T?, T, T?) -> Unit) =
  forEachWithSurrounding { prev, curr, next, _ -> block(prev, curr, next) }

fun <T> List<T>.forEachWithSurroundingCyclical(block: (T, T, T, Int) -> Unit) = forEachIndexed { index, item ->
  val prevItem = if (index == 0) this[size - 1] else this[index - 1]
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(prevItem, item, nextItem, index)
}

fun <T> List<T>.forEachWithSurroundingCyclical(block: (T, T, T) -> Unit) =
  forEachWithSurroundingCyclical { prev, curr, next, _ -> block(prev, curr, next) }

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

fun <T, R> List<T>.mapWithSurrounding(block: (T?, T, T?, Int) -> R) = mapIndexed { index, item ->
  val prevItem = if (index == 0) null else this[index - 1]
  val nextItem = if (index == size - 1) null else this[index + 1]
  block(prevItem, item, nextItem, index)
}

fun <T, R> List<T>.mapWithSurrounding(block: (T?, T, T?) -> R) = mapWithSurrounding { prev, curr, next, index ->
  block(prev, curr, next)
}

fun <T, R> List<T>.mapWithSurroundingCyclical(block: (T, T, T, Int) -> R) = mapIndexed { index, item ->
  val prevItem = if (index == 0) this[size - 1] else this[index - 1]
  val nextItem = if (index == size - 1) this[0] else this[index + 1]
  block(prevItem, item, nextItem, index)
}

fun <T, R> List<T>.mapWithSurroundingCyclical(block: (T, T, T) -> R) = mapWithSurroundingCyclical { prev, curr, next, index ->
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

fun <T> List<T>.zipWithSiblings() = mapWithSurrounding { prev, curr, next -> Triple(prev, curr, next) }

fun <T> List<T>.zipWithNext(): List<Pair<T, T>> = mapWithNext { curr, next -> Pair(curr, next) }

fun <T> List<T>.zipWithSiblingsCyclical() = mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }