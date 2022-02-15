package util.concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.streams.toList

fun <A, B> List<A>.parallelStreamMap(f: (A) -> B): List<B> = parallelStream().map(f).toList()

fun <A, B> List<A>.pmap(f: suspend (A) -> B): List<B> = runBlocking {
  map { async(Dispatchers.Default) { f(it) } }.map { it.await() }
}
