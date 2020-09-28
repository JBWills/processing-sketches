package util

fun <T> Iterable<T>.pEach() = println(map { it.toString() })