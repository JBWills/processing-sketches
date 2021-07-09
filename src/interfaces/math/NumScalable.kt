package interfaces.math

interface NumScalable<T> {
  operator fun times(other: Number): T
  operator fun div(other: Number): T
}
