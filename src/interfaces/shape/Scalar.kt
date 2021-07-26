package interfaces.shape

import arrow.core.compareTo
import interfaces.math.Mathable
import util.roundedString
import util.squared
import kotlin.math.sqrt

interface Scalar<T : Scalar<T>> :
  Comparable<T>,
  Mathable<T>,
  Transformable<T> {
  val values: List<Double>

  operator fun get(index: Int): Double = values[index]

  fun fromValues(values: List<Double>): T

  fun fromValues(vararg values: Double): T = fromValues(values.toList())


  val magnitude get() = sqrt(magnitudeSquared)

  // useful when you don't want to do expensive sqrt() calcs
  val magnitudeSquared get() = values.sumOf { it.squared() }

  fun dist(other: T) = (this - other).magnitude

  fun distSquared(other: T) = (this - other).magnitudeSquared

  val normalized: T
    get() {
      val magnitudeSquared = magnitudeSquared
      return if (magnitudeSquared == 0.0) fromValues(
        ArrayList<Double>(values.size).also {
          if (it.size > 0) it[0] = 1.0
        },
      )
      else div(magnitudeSquared)
    }

  override operator fun compareTo(other: T): Int = values.compareTo(other.values)

  override operator fun unaryMinus() = fromValues(values.map { -it })

  override operator fun unaryPlus() = fromValues(values.map { +it })

  operator fun plus(other: List<T>) = listOf(this) + other
  override operator fun plus(other: Number) =
    fromValues(values.map { it + other.toDouble() })

  operator fun plus(other: T): T =
    fromValues(values.mapIndexed { index, thisVal -> thisVal + other[index] })

  override operator fun minus(other: Number) = this + -other.toDouble()
  operator fun minus(other: T): T =
    fromValues(values.mapIndexed { index, thisVal -> thisVal - other[index] })

  override operator fun div(other: Number) = fromValues(values.map { it / other.toDouble() })
  operator fun div(other: T): T =
    fromValues(values.mapIndexed { index, thisVal -> thisVal / other[index] })

  override operator fun times(other: Number) = fromValues(values.map { it * other.toDouble() })
  operator fun times(other: T): T =
    fromValues(values.mapIndexed { index, thisVal -> thisVal * other[index] })

  fun toString(name: String = "Scalar", decimalPrecision: Int = 5): String {
    return "$name(${
      values.mapIndexed { dimension, value ->
        "${getDimensionName(dimension)} = ${value.roundedString(decimalPrecision)},"
      }.joinToString(",")
    })"
  }

  fun getDimensionName(dim: Int): String = when (dim) {
    0 -> "x"
    1 -> "y"
    2 -> "z"
    else -> "$dim"
  }

  companion object {
    operator fun <T : Scalar<T>> Number.times(p: T): T = p * this
    operator fun <T : Scalar<T>> Number.plus(p: T): T = p + this
    operator fun <T : Scalar<T>> Number.div(p: T): T =
      p.fromValues(p.values.map { this.toDouble() / it })

    operator fun Number.minus(p: Scalar<*>) =
      p.fromValues(p.values.map { this.toDouble() - it })
  }
}
