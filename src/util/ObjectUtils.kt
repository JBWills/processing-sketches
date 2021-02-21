package util

inline fun <A, R> A.letWith(block: A.() -> R) = let { it.block() }
