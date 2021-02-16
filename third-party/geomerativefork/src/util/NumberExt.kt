package geomerativefork.src.util

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

fun Double.bound(start: Double = 0.0, end: Double = 1.0): Double = when {
  this < start -> start
  this > end -> end
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
