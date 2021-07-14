package util

fun <T> T.alsoDebugLog(): T = also { debugLog(it) }

fun debugLog(vararg items: Any?, separator: String = " ") =
  println(items.joinToString(separator = separator))

fun debugLogLines(vararg items: Any?) = println(items.joinToString(separator = "\n"))
