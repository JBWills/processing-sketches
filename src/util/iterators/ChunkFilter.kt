package util.iterators

data class ChunkCursor<T>(
  val previous: T?,
  val value: T,
  val next: T?,
  val index: Number,
)

/**
 * Iterate over list, evaluating each item as in predicate or not, if it is not, split the list and
 * discard the unmatched elements.
 *
 * @param T The type of the list
 * @param predicate true if the item should be in the result
 * @return a list of lists whose summed size is no larger than the original list
 */
fun <T> Iterable<T>.chunkFilter(predicate: (ChunkCursor<T>) -> Boolean): List<List<T>> {
  var lastInside: Boolean? = null

  val result = mutableListOf<MutableList<T>>()
  forEachWithSurroundingIndexed { index, prev, value, next ->
    val inside = predicate(ChunkCursor(prev, value, next, index))
    if (inside) {
      if (inside != lastInside) result.add(mutableListOf())
      result.last().add(value)
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
  predicate: (ChunkCursor<T>) -> Boolean,
  getBoundaryValue: (Pair<ChunkCursor<T>, Boolean>, Pair<ChunkCursor<T>, Boolean>) -> T?
): List<List<T>> {
  var lastValue: Pair<ChunkCursor<T>, Boolean>? = null

  val result = mutableListOf<MutableList<T>>()

  forEachWithSurroundingIndexed { index, prev, item, next ->
    val cursor = ChunkCursor(prev, item, next, index)
    val inside = predicate(cursor)
    if (inside != lastValue?.second) {
      result.add(mutableListOf())

      lastValue?.let { (lastCursor, lastInside) ->
        getBoundaryValue(Pair(lastCursor, lastInside), Pair(cursor, inside))
          ?.let { boundaryItem ->
            val listToAddBoundaryItem = if (inside) result.last() else result[result.size - 2]
            listToAddBoundaryItem.add(boundaryItem)
          }
      }
    }

    if (inside) result.last().add(item)

    lastValue = cursor to inside
  }

  return result
}
