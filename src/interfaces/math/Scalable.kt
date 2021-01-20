package interfaces.math

interface Scalable<T> {
  operator fun times(other: Number): T
  operator fun div(other: Number): T
}