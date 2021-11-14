package util.numbers

import util.DoubleRange
import kotlin.math.max
import kotlin.math.min

fun Int.bound(range: IntRange): Int = when {
  this < range.first -> range.first
  this > range.last -> range.last
  else -> this
}

fun Double.bound(range: DoubleRange): Double = when {
  this < range.start -> range.start
  this > range.endInclusive -> range.endInclusive
  else -> this
}

fun Float.boundInt(start: Int = 0, end: Int = 1): Int = when {
  this < start -> start
  this > end -> end
  else -> this.toInt()
}

fun Double.boundInt(start: Int = 0, end: Int = 1): Int = when {
  this < start -> start
  this > end -> end
  else -> this.toInt()
}

fun Float.bound(start: Float = 0f, end: Float = 1f): Float = when {
  this < start -> start
  this > end -> end
  else -> this
}

fun Double.bound(start: Number = 0.0, end: Number = 1.0): Double = when {
  this < start.toDouble() -> start.toDouble()
  this > end.toDouble() -> end.toDouble()
  else -> this
}

fun Int.bound(start: Int = 0, end: Int = 1): Int = when {
  this < start -> start
  this > end -> end
  else -> this
}

fun Number.bound(start: Float = 0f, end: Float = 1f): Float {
  val t = this.toFloat()
  return when {
    t < start.toDouble() -> start
    t > end.toDouble() -> end
    else -> t
  }
}

fun Double.boundMin(min: Number = 0): Double = max(this, min.toDouble())
fun Double.boundMax(max: Number = 1): Double = min(this, max.toDouble())
fun Int.boundMin(min: Number = 0): Int = max(min.toInt(), this)
fun Int.boundMax(max: Number = 1): Int = min(this, max.toInt())
fun Float.boundMin(min: Number = 0): Float = max(min.toFloat(), this)
fun Float.boundMax(max: Number = 1): Float = min(this, max.toFloat())
