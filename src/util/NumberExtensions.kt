package util

import kotlin.math.cos
import kotlin.math.sin

fun Int.times(f: (Int) -> Unit) {
  for (i in 0.rangeTo(this)) f(i)
}

fun <T : Number> T.sin(): Float = sin(toDouble()).toFloat()
fun <T : Number> T.cos(): Float = cos(toDouble()).toFloat()
fun Int.squared(): Int = this * this
fun Int.pow(other: Int): Int = Math.pow(this.toDouble(), other.toDouble()).toInt()
fun Int.pow(other: Number): Float = Math.pow(this.toDouble(), other.toDouble()).toFloat()
fun Float.squared(): Float = this * this