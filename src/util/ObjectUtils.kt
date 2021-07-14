package util

inline fun <A, R> A.letWith(block: A.() -> R) = let { it.block() }
inline fun <A> A.doIf(predicate: Boolean, block: (A) -> A): A =
  if (predicate) block(this) else this

fun Any.asCollection(): Collection<*> = this as Collection<*>
