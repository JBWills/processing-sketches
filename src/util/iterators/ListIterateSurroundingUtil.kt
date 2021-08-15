package util.iterators

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

fun <T, R> List<T>.mapWithNextIndexed(block: (Int, T, T) -> R) =
  mapWithSurrounding { _, curr, next, index ->
    next?.let { block(index, curr, next) }
  }.filterNotNull()

fun <T> List<T>.zipWithSiblings(): List<Triple<T?, T, T?>> =
  mapWithSurrounding { prev, curr, next -> Triple(prev, curr, next) }

fun <T> List<T>.zipWithNext(): List<Pair<T, T>> = mapWithNext { curr, next -> Pair(curr, next) }

fun <T> List<T>.zipWithSiblingsCyclical() =
  mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }
