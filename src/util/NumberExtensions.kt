package util

import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

fun Int.times(f: (Int) -> Unit) {
  for (i in 0.rangeTo(this)) f(i)
}

fun <T : Number> T.sin(): Float = sin(toDouble()).toFloat()
fun <T : Number> T.cos(): Float = cos(toDouble()).toFloat()
fun Int.squared(): Int = this * this
fun Int.pow(other: Int): Int = toDouble().pow(other.toDouble()).toInt()
fun Int.pow(other: Number): Float = toDouble().pow(other.toDouble()).toFloat()
fun Float.squared(): Float = this * this

fun Number.degreesToRadians(): Float = toRadians(toDouble()).toFloat()

fun Number.inchesToPx(): Int = (this.toFloat() * 72f).toInt()