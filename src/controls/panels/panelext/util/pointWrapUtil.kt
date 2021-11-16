package controls.panels.panelext.util

import coordinate.Point
import util.base.DoubleRange
import kotlin.reflect.KMutableProperty0

@JvmName("pointWrappedDoublePair")
fun pointWrapped(d1: KMutableProperty0<Double>, d2: KMutableProperty0<Double>) = (d1 to d2).wrapped(
  wrap = { x, y -> Point(x, y) },
  unwrap = { x to y },
)

fun pointWrapped(d1: KMutableProperty0<Int>, d2: KMutableProperty0<Int>) = (d1 to d2).wrapped(
  wrap = { x, y -> Point(x, y) },
  unwrap = { x.toInt() to y.toInt() },
)

@JvmName("pointWrappedDoubleRange")
fun KMutableProperty0<DoubleRange>.pointWrapped() = wrapped(
  wrap = { Point(start, endInclusive) },
  unwrap = { x..y },
)

@JvmName("pointWrappedIntRange")
fun KMutableProperty0<IntRange>.pointWrapped() = wrapped(
  wrap = { Point(start, endInclusive) },
  unwrap = { x.toInt()..y.toInt() },
)

@JvmName("pointWrappedDoublePair")
fun KMutableProperty0<Pair<Double, Double>>.pointWrapped() = wrapped(
  wrap = { Point(first, second) },
  unwrap = { x to y },
)

@JvmName("pointWrappedIntPair")
fun KMutableProperty0<Pair<Int, Int>>.pointWrapped() = wrapped(
  wrap = { Point(first, second) },
  unwrap = { x.toInt() to y.toInt() },
)
