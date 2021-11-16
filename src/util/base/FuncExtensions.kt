package util.base

fun <T> T.with(block: T.() -> Unit) = block(this)
