package util.iterators

/**
 * Iterate over list, evaluating each item as in predicate or not, if it is not, split the list and
 * discard the unmatched elements.
 *
 * @param T The type of the list
 * @param predicate true if the item should be in the result
 * @return a list of lists whos summed size is no larger than the original list
 */
fun <T> Iterable<T>.chunkFilter(predicate: (T) -> Boolean): List<List<T>> {
  var lastInside: Boolean? = null

  val result = mutableListOf<MutableList<T>>()
  forEach {
    val inside = predicate(it)
    if (inside) {
      if (inside != lastInside) result.add(mutableListOf())
      result.last().add(it)
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
  predicate: (T) -> Boolean,
  getBoundaryValue: (Triple<T, Int, Boolean>, Triple<T, Int, Boolean>) -> T?
): List<List<T>> {
  var lastValue: Pair<T, Boolean>? = null

  val result = mutableListOf<MutableList<T>>()

  forEachIndexed { index, item ->
    val inside = predicate(item)
    if (inside != lastValue?.second) {
      result.add(mutableListOf())

      lastValue?.let { (lastItem, lastInside) ->
        getBoundaryValue(
          Triple(lastItem, index - 1, lastInside),
          Triple(item, index, inside),
        )?.let { boundaryItem ->
          val listToAddBoundaryItem = if (inside) result.last() else result[result.size - 2]
          listToAddBoundaryItem.add(boundaryItem)
        }
      }
    }

    if (inside) result.last().add(item)

    lastValue = item to inside
  }

  return result
}
