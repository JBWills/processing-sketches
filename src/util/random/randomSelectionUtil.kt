package util.random

import kotlin.random.Random

fun Random.randomWeightedIndex(doubles: List<Double>): Int {
  val total = doubles.sum()
  val randomNumber = nextDouble() * total

  var runningTotal = 0.0

  doubles.forEachIndexed { index, weight ->
    runningTotal += weight
    if (randomNumber <= runningTotal) return index
  }

  return -1
}
