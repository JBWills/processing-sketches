package util.base

inline fun <T> T.with(block: T.() -> Unit) = block(this)
