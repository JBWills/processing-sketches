package util.numbers

fun minMax(d1: Double, d2: Double): Pair<Double, Double> = if (d1 < d2) d1 to d2 else d2 to d1
fun minMax(d1: Int, d2: Int): Pair<Int, Int> = if (d1 < d2) d1 to d2 else d2 to d1
