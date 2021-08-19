package util


fun Any.asCollection(): Collection<*> = this as Collection<*>

fun <T> T.listWrapped(): List<T> = listOf(this)
fun <T> T.setWrapped(): Set<T> = setOf(this)
inline fun <reified T> T.arrayWrapped(): Array<T> = arrayOf(this)
