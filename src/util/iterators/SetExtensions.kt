package util.iterators

/**
 * Get an element from the set.
 *
 * Element will be the "first" element in the set, but sets are unordered so
 * don't expect it to be ordered or uniformly random or anything.
 *
 * Throws an exception if the set is empty.
 */
fun <T> Set<T>.get(): T = elementAt(0)

/**
 * Get an element from the set and remove it.
 *
 * Element will be the "first" element in the set, but sets are unordered so
 * don't expect it to be ordered or uniformly random or anything.
 *
 * Throws an exception if the set is empty.
 */
fun <T> MutableSet<T>.pop(): T = elementAt(0).also { remove(it) }
