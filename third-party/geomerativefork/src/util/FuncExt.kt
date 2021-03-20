package geomerativefork.src.util

fun <T> T.with(block: T.() -> Unit) = block(this)
