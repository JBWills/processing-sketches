package util

@Suppress("unused")
fun <T> T.alsoDebugLog(): T = also { debugLog(it) }

fun debugLog(vararg items: Any?, separator: String = " ") =
  println(items.joinToString(separator = separator))

@Suppress("unused")
fun debugLogLines(vararg items: Any?) = println(items.joinToString(separator = "\n"))
