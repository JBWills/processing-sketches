package util

typealias DoubleRange = ClosedRange<Double>

class DoubleProgression(
  override val start: Double,
  override val endInclusive: Double,
  private val step: Double = 1.0,
) : Iterable<Double>, DoubleRange {
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

infix fun DoubleRange.step(s: Double) = DoubleProgression(start, endInclusive) step s

fun DoubleRange.at(amountAlong: Double = 0.0) = start + ((endInclusive - start) * amountAlong)
fun DoubleRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)