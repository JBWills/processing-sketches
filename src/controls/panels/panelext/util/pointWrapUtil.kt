package controls.panels.panelext.util

import coordinate.Point
import util.DoubleRange
import kotlin.reflect.KMutableProperty0

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
