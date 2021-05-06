package util

import kotlin.math.abs

typealias DoubleRange = ClosedRange<Double>

operator fun DoubleRange.times(other: Number) =
  (start * other.toDouble())..(endInclusive * other.toDouble())

operator fun DoubleRange.plus(other: Number) =
  (start + other.toDouble())..(endInclusive + other.toDouble())

operator fun DoubleRange.minus(other: Number) =
  (start - other.toDouble())..(endInclusive - other.toDouble())

operator fun DoubleRange.div(other: Number) =
  (start / other.toDouble())..(endInclusive / other.toDouble())

class DoubleProgression(
  startNumber: Number,
  endInclusiveNumber: Number,
  private val step: Double = 1.0,
) : Iterable<Double>, DoubleRange {
  override val start: Double = startNumber.toDouble()
  override val endInclusive: Double = endInclusiveNumber.toDouble()

  override fun iterator(): Iterator<Double> = DoubleIterator(start, endInclusive, step)

  infix fun step(moveAmount: Double) = DoubleProgression(start, endInclusive, moveAmount)
}

class DoubleIterator(
  private val start: Double,
  private val endInclusive: Double,
  private val stepVal: Double,
) : Iterator<Double> {

  private var curr: Double? = null

  private fun getNext(c: Double?): Double {
    if (c == null) return start

    val next = c + stepVal
    if (isPastEnd(next)) {
      return endInclusive
    }

    return next
  }

  private fun getNext(): Double = getNext(curr)

  private fun isPastEnd(p: Double) = abs(start - endInclusive) < abs(start - p)

  override fun hasNext() = curr?.let { it != endInclusive && !isPastEnd(it) } ?: true

  override fun next(): Double {
    val next = getNext()
    curr = next
    return next
  }
}

val ZeroToOne = 0.0..1.0
val NegativeOneToOne = -1.0..1.0

fun zeroTo(max: Number) = 0.0..max.toDouble()
fun negToPos(minMax: Number) = -minMax.toDouble()..minMax.toDouble()

infix fun DoubleRange.step(s: Double): DoubleProgression =
  DoubleProgression(start, endInclusive) step s

infix fun DoubleRange.numSteps(numSteps: Number) =
  step((endInclusive - start) / (numSteps.toDouble() - 1))

infix fun Double.until(s: Double) = DoubleProgression(this, s - 1)

fun DoubleRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun PointRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun IntRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun DoubleRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
fun IntRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
