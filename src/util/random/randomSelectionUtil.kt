package util.random

import util.rand

fun List<Double>.randomWeightedIndex(seed: Int): Int {
  val total = sum()
  val randomNumber = rand(seed) * total

  var runningTotal = 0.0

  forEachIndexed { index, weight ->
    runningTotal += weight
    if (randomNumber <= runningTotal) return index
  }
  
  return -1
}
