package util

import interfaces.math.Addable
import interfaces.math.Scalable
import interfaces.math.Subtractable

fun IntRange.toDoubleRange() = first.toDouble()..last.toDouble()

fun ClosedRange<Int>.toIntRange(): IntRange = start..endInclusive

operator fun <T> ClosedRange<T>.times(
  other: Number,
): ClosedRange<T> where T : Scalable<T>, T : Comparable<T> =
  (start * other)..(endInclusive * other)

operator fun <T> ClosedRange<T>.plus(
  other: Number,
): ClosedRange<T> where T : Addable<T>, T : Comparable<T> =
  (start + other)..(endInclusive + other)

operator fun <T> ClosedRange<T>.minus(
  other: Number,
): ClosedRange<T> where T : Subtractable<T>, T : Comparable<T> =
  (start - other)..(endInclusive - other)

operator fun <T> ClosedRange<T>.unaryMinus(): ClosedRange<T> where T : Subtractable<T>, T : Comparable<T> =
  (-start)..(-endInclusive)

class RangeWithCurrent<T : Comparable<T>>(val range: ClosedRange<T>, val value: T) {
  companion object {
    infix fun <E : Comparable<E>> ClosedRange<E>.at(value: E) = RangeWithCurrent(this, value)
    infix fun ClosedRange<Double>.at(value: Number) = RangeWithCurrent(this, value.toDouble())
  }
}

