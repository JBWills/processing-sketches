package util.iterators

fun <T> List<T>.zipWithSiblings(): List<Triple<T?, T, T?>> =
  mapWithSurrounding { prev, curr, next -> Triple(prev, curr, next) }

fun <T> List<T>.zipWithNext(): List<Pair<T, T>> =
  mapWithNext { curr, next -> next?.let { Pair(curr, it) } }.filterNotNull()

fun <T> List<T>.zipWithSiblingsCyclical() =
  mapWithSurroundingCyclical { prev, curr, next -> Triple(prev, curr, next) }
