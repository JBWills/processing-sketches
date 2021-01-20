package interfaces.math

interface Subtractable<T> {
  operator fun minus(other: Number): T
  operator fun unaryMinus(): T
}