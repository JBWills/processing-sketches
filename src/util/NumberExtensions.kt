package util

fun Int.times(f: (Int) -> Unit) {
  for (i in 0.rangeTo(this)) f(i)
}