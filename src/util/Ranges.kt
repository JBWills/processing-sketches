package util

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

  override fun iterator(): Iterator<Double> =
    if (step > 0) DoubleIterator(start, endInclusive, step)
    else DoubleIterator(endInclusive, start, -step)

  infix fun step(moveAmount: Double) = DoubleProgression(start, endInclusive, moveAmount)
}

class DoubleIterator(
  private val start: Double,
  private val endInclusive: Double,
  val stepVal: Double,
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

  private fun isPastEnd(p: Double) = start - endInclusive < start - p

  override fun hasNext() = curr == null || start - endInclusive > start - curr!!

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

infix fun DoubleRange.step(s: Double) = DoubleProgression(start, endInclusive) step s

fun DoubleRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun IntRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun DoubleRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
fun IntRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
