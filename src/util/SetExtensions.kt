package util

import kotlin.random.Random


fun <T> Set<T>.pick(num: Int, seed: Int = 100): Set<T> {
  if (num > size) return this
  val res = mutableSetOf<T>()
  val mutableThis = toMutableSet()
  num.times {
    val elem = mutableThis.random(Random(seed))
    res.add(elem)
    mutableThis.remove(elem)
  }

  return res
}
