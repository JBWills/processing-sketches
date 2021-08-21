package util

inline fun <A, R> A.letWith(block: A.() -> R) = let { it.block() }
inline fun <A> A.doIf(predicate: Boolean, block: (A) -> A): A =
  doIf(predicate, ifTrue = block) { this }

inline fun <A, B> A.doIf(predicate: Boolean, ifTrue: (A) -> B, ifFalse: (A) -> B): B =
  if (predicate) ifTrue(this) else ifFalse(this)

inline fun <A> Iterable<A>.mapIf(predicate: Boolean, block: (A) -> A): List<A> =
  if (predicate) map(block) else toList()

inline fun <A, B> Iterable<A>.mapIf(
  predicate: Boolean,
  ifTrue: (A) -> B,
  ifFalse: (A) -> B
): List<B> =
  if (predicate) map(ifTrue) else map(ifFalse)

fun <A> Iterable<A>.maybeMap(block: ((A) -> A)?): List<A> =
  if (block == null) toList() else map(block)

fun <A> Array<A>.maybeMap(block: ((A) -> A)?): List<A> =
  if (block == null) toList() else map(block)

inline fun <A, B> A.doIf(predicate: Boolean, or: B, block: (A) -> B): B =
  if (predicate) block(this) else or

inline fun <A, B, T> letNonNull(a: A?, b: B?, block: (A, B) -> T): T? {
  return if (a == null || b == null) null
  else block(a, b)
}

inline fun <A, B, C, T> letNonNull(a: A?, b: B?, c: C?, block: (A, B, C) -> T): T? {
  return if (a == null || b == null || c == null) null
  else block(a, b, c)
}
