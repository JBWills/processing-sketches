package util.timing

import kotlin.system.measureTimeMillis

private fun getTimeString(millis: Long) = when {
  millis < 1000 -> "${millis}ms"
  else -> "${millis / 1000}s"
}

fun <T, K> T.timed(block: T.() -> K): Pair<Long, K> {
  var result: K
  val millis = measureTimeMillis { result = block() }

  return Pair(millis, result)
}

fun <T, K> T.printTime(prefix: String? = null, block: T.() -> K): K {
  var result: K
  val millis = measureTimeMillis { result = block() }

  println("${prefix ?: ""} took ${getTimeString(millis)}")
  return result
}
