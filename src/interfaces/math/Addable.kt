package interfaces.math

interface Addable<T> {
  operator fun plus(other: Number): T
  operator fun unaryPlus(): T
}