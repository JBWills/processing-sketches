package util.numbers

import coordinate.Point
import freemarker.template.utility.NumberUtil.isNaN
import util.atAmountAlong
import util.base.DoubleRange
import util.base.step
import util.percentAlong
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

const val EPSILON = 0.00001

fun forEachPoint(w: Double, h: Double, step: Double, block: (p: Point) -> Unit) =
  (0.0..h step step).forEach { y ->
    (0.0..w step step).forEach { x ->
      block(Point(x, y))
    }
  }

inline fun <R> Int.map(f: (Int) -> R) = (0 until this).map(f)

inline fun Int.times(f: (Int) -> Unit) = (0 until this).forEach(f)

fun sum(vararg numbers: Number): Double =
  numbers.sumOf { it.toDouble() }

fun mean(vararg numbers: Number): Double = sum(*numbers) / numbers.size

val Int.odd: Boolean get() = !even
val Int.even: Boolean get() = this % 2 == 0

fun Number.coerceOdd(roundUp: Boolean = true): Int {
  val numberInt = toDouble().floorInt()
  if (numberInt.even) {
    return if (roundUp) numberInt + 1 else numberInt - 1
  }

  return numberInt
}

fun Number.coerceEven(roundUp: Boolean = true): Int {
  val numberInt = toDouble().floorInt()
  if (numberInt.odd) {
    return if (roundUp) numberInt + 1 else numberInt - 1
  }

  return numberInt
}

fun Double.floorInt(): Int = floor(this).toInt()
fun Double.ceilInt(): Int = ceil(this).toInt()

/**
 * If number basically equals other (within threshold amount), just make it
 * equal to the other number. Useful to avoid NaN issues with ever so slightly
 * negative util.numbers that are basically 0.
 */
fun Double.coerceTo(other: Number, threshold: Double = EPSILON): Double =
  if (equalsDelta(other, threshold)) other.toDouble() else this

fun Number.remap(fromRange: DoubleRange, toRange: DoubleRange) =
  toRange.atAmountAlong(fromRange.percentAlong(this))

fun Double.roundedString(decimals: Int = 2) = "%.${decimals}f".format(this)

fun Number.equalsDelta(other: Number, threshold: Double = EPSILON) =
  abs(this.toDouble() - other.toDouble()) < threshold

fun Number.notEqualsDelta(other: Number, threshold: Double = EPSILON) =
  !equalsDelta(other, threshold)

fun Number.lessThanEqualToDelta(other: Number, threshold: Double = EPSILON) =
  this.toDouble() <= other.toDouble() + threshold

fun Number.lessThan(other: Number, threshold: Double = EPSILON) =
  notEqualsDelta(other, threshold) && lessThanEqualToDelta(other, threshold)

fun Number.greaterThanEqualToDelta(other: Number, threshold: Double = EPSILON) =
  this.toDouble() >= other.toDouble() + threshold

fun Number.equalsZero() = equalsDelta(0)
fun Number.notEqualsZero() = !equalsZero()

fun <T : Number> T.sin(): Double = sin(toDouble())
fun <T : Number> T.cos(): Double = cos(toDouble())
fun <T : Number> T.tan(): Double = tan(toDouble())
fun Int.squared(): Int = this * this
fun Int.pow(other: Int): Int = toDouble().pow(other.toDouble()).toInt()
fun Int.pow(other: Number): Double = toDouble().pow(other.toDouble())
fun Number.pow(other: Number): Double = toDouble().pow(other.toDouble())
fun Double.squared(): Double = this * this
fun Float.squared(): Float = this * this
fun Double.sqrt(): Double = kotlin.math.sqrt(this)

fun Number.toRadians(): Double = toRadians(toDouble())
fun Number.toDegrees(): Double = toDegrees(toDouble())

fun Number.inchesToPx(): Int = (this.toDouble() * 72.0).toInt()

fun Int.ifNan(block: () -> Int): Int = if (isNaN(this)) block() else this
fun Float.ifNan(block: () -> Float): Float = if (isNaN(this)) block() else this
fun Double.ifNan(block: () -> Double): Double = if (isNaN(this)) block() else this

fun min(vararg doubles: Double) = doubles.reduce { acc, item -> kotlin.math.min(acc, item) }
