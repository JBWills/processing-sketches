package util.iterators

//
//
//  Non-cyclical
//
//

inline fun <T> Iterable<T>.forEachWithSurroundingIndexed(block: (Int, T?, T, T?) -> Unit) {
  var previous: T? = null
  var curr: T? = null
  var next: T? = null

  var index = -1
  forEach { item ->
    previous = curr
    curr = next
    next = item

    curr?.let { block(index, previous, it, next) }
    index++
  }

  next?.let { block(index, curr, it, null) }
}

inline fun <T> Iterable<T>.forEachWithSurrounding(block: (T?, T, T?) -> Unit) =
  forEachWithSurroundingIndexed { _, prev, curr, next -> block(prev, curr, next) }

inline fun <T, R> Iterable<T>.mapWithSurroundingIndexed(block: (Int, T?, T, T?) -> R): List<R> {
  val result = mutableListOf<R>()

  forEachWithSurroundingIndexed { index, prev, curr, next ->
    result.add(block(index, prev, curr, next))
  }

  return result
}

inline fun <T, R> Iterable<T>.mapWithSurrounding(block: (T?, T, T?) -> R): List<R> =
  mapWithSurroundingIndexed { _, prev, curr, next -> block(prev, curr, next) }

inline fun <T, R> Iterable<T>.mapWithPrev(block: (T?, T) -> R): List<R> =
  mapWithSurroundingIndexed { _, prev, curr, _ -> block(prev, curr) }

inline fun <T, R> Iterable<T>.mapWithNext(block: (T, T?) -> R): List<R> =
  mapWithSurroundingIndexed { _, _, curr, next -> block(curr, next) }

inline fun <T, R> Iterable<T>.mapWithPrevIndexed(block: (Int, T?, T) -> R): List<R> =
  mapWithSurroundingIndexed { index, prev, curr, _ -> block(index, prev, curr) }

inline fun <T, R> Iterable<T>.mapWithNextIndexed(block: (Int, T, T?) -> R): List<R> =
  mapWithSurroundingIndexed { index, _, curr, next -> block(index, curr, next) }

//
//
//  Cyclical
//  For the first iteration, include the last element as "prev" and for the last iteration,
//  Include the first element as "next"
//
//

inline fun <T> Iterable<T>.forEachWithSurroundingIndexedCyclical(block: (Int, T, T, T) -> Unit) {
  var previous: T? = null
  var curr: T? = null
  var next: T? = null

  val firstItem = firstOrNull() ?: return
  val lastItem = lastOrNull() ?: return

  var index = -1
  forEach { item ->
    previous = curr
    curr = next
    next = item

    curr?.let { block(index, previous ?: lastItem, it, next ?: firstItem) }
    index++
  }

  next?.let { block(index, curr ?: lastItem, it, next ?: firstItem) }
}

inline fun <T> Iterable<T>.forEachWithSurroundingCyclical(block: (T, T, T) -> Unit) =
  forEachWithSurroundingIndexedCyclical { _, prev, curr, next -> block(prev, curr, next) }

inline fun <T, R> Iterable<T>.mapWithSurroundingIndexedCyclical(block: (Int, T, T, T) -> R): List<R> {
  val result = mutableListOf<R>()

  forEachWithSurroundingIndexedCyclical { index, prev, curr, next ->
    result.add(block(index, prev, curr, next))
  }

  return result
}

inline fun <T, R> Iterable<T>.mapWithSurroundingCyclical(block: (T, T, T) -> R): List<R> =
  mapWithSurroundingIndexedCyclical { _, prev, curr, next -> block(prev, curr, next) }

inline fun <T, R> Iterable<T>.mapWithPrevCyclical(block: (T, T) -> R): List<R> =
  mapWithSurroundingIndexedCyclical { _, prev, curr, _ -> block(prev, curr) }

inline fun <T, R> Iterable<T>.mapWithNextCyclical(block: (T, T) -> R): List<R> =
  mapWithSurroundingIndexedCyclical { _, _, curr, next -> block(curr, next) }

inline fun <T, R> Iterable<T>.mapWithPrevIndexedCyclical(block: (Int, T, T) -> R): List<R> =
  mapWithSurroundingIndexedCyclical { index, prev, curr, _ -> block(index, prev, curr) }

inline fun <T, R> Iterable<T>.mapWithNextIndexedCyclical(block: (Int, T, T) -> R): List<R> =
  mapWithSurroundingIndexedCyclical { index, _, curr, next -> block(index, curr, next) }


//
//
//  Non-Null
//  Only iterate over pairs that exist
//
//

inline fun <T, R> Iterable<T>.forEachSiblingIndexed(block: (Int, T, T) -> R) {
  forEachWithSurroundingIndexed { index, _, curr, next ->
    next?.let { block(index, curr, next) }
  }
}

inline fun <T, R> Iterable<T>.forEachSibling(block: (T, T) -> R) {
  forEachSiblingIndexed { _, curr, next -> block(curr, next) }
}

inline fun <T, R> Iterable<T>.mapWithSiblingIndexed(block: (Int, T, T) -> R): List<R> {
  val result = mutableListOf<R>()

  forEachSiblingIndexed { index, curr, next ->
    result.add(block(index, curr, next))
  }

  return result
}

inline fun <T, R> Iterable<T>.mapWithSibling(block: (T, T) -> R): List<R> {
  val result = mutableListOf<R>()

  forEachWithSurroundingIndexed { _, _, curr, next ->
    next?.let {
      result.add(block(curr, next))
    }
  }

  return result
}
