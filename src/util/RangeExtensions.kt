package util

import coordinate.Point
import interfaces.math.Addable
import interfaces.math.NumScalable
import interfaces.math.Subtractable

fun IntRange.toDoubleRange() = first.toDouble()..last.toDouble()

fun ClosedRange<Int>.toIntRange(): IntRange = start..endInclusive

operator fun <T> ClosedRange<T>.times(
  other: Number,
): ClosedRange<T> where T : NumScalable<T>, T : Comparable<T> =
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

fun DoubleRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun PointRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun IntRange.atAmountAlong(amountAlong: Double = 0.0) =
  start + ((endInclusive - start) * amountAlong)

fun DoubleRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
fun IntRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)
fun PointRange.percentAlong(p: Point) = (p - start) / (endInclusive - start)
fun Number.mappedTo(r: IntRange) = r.atAmountAlong(toDouble())
fun Number.mappedTo(r: DoubleRange) = r.atAmountAlong(toDouble())
fun Number.mappedTo(r: PointRange) = r.atAmountAlong(toDouble())
fun Number.percentAlong(r: IntRange) = r.percentAlong(toDouble())
fun Number.percentAlong(r: DoubleRange) = r.percentAlong(toDouble())
fun Point.percentAlong(r: PointRange) = r.percentAlong(this)
