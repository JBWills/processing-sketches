package geomerativefork.src.util

import util.DoubleRange
import kotlin.math.max
import kotlin.math.min

fun minAll(vararg floats: Float) = floats.reduce { acc, item -> min(acc, item) }

fun maxAll(vararg floats: Float) = floats.reduce { acc, item -> max(acc, item) }

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

fun Double.boundMin(min: Double = 0.0): Double = max(min, this)
fun Double.boundMax(max: Double = 1.0): Double = min(this, max)
fun Int.boundMin(min: Int = 0): Int = max(min, this)
fun Int.boundMax(max: Int = 1): Int = min(this, max)
fun Float.boundMin(min: Float = 0f): Float = max(min, this)
fun Float.boundMax(max: Float = 1f): Float = min(this, max)

fun Number.toDoubleArray() = doubleArrayOf(toDouble())
fun Number.toFloatArray() = floatArrayOf(toFloat())
fun Number.toIntArray() = intArrayOf(toInt())
