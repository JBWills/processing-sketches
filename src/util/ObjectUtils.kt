package util

inline fun <A, R> A.letWith(block: A.() -> R) = let { it.block() }
inline fun <A> A.doIf(predicate: Boolean, block: (A) -> A): A =
  if (predicate) block(this) else this

inline fun <A, B> A.doIf(predicate: Boolean, or: B, block: (A) -> B): B =
  if (predicate) block(this) else or

fun Any.asCollection(): Collection<*> = this as Collection<*>
