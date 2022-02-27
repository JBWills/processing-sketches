@file:Suppress("unused")

package util.timing

import java.lang.System.currentTimeMillis
import java.util.concurrent.ConcurrentHashMap
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

val timers: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

fun <T> T.timeStart(key: String): T = apply {
  timers[key] = currentTimeMillis()
}

fun <T> T.timeEndAndPrint(key: String): T = apply {
  timers[key]?.also {
    println("Timer $key took: ${getTimeString(currentTimeMillis() - it)}")
    timers.remove(key)
  } ?: println("Could not find timer with key: $key")
}
