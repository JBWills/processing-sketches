package util

typealias DoubleRange = ClosedRange<Double>

fun DoubleRange.at(amountAlong: Double = 0.0) = start + ((endInclusive - start) * amountAlong)
fun DoubleRange.percentAlong(num: Number) = (num.toDouble() - start) / (endInclusive - start)