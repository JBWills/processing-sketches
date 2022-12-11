package util.iterators

fun Any.asCollection(): Collection<*> = this as Collection<*>

fun <T> T.listWrapped(): List<T> = listOf(this)
