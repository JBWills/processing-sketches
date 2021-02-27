package util.tuple

/* Times section */
@JvmName("doublePairTimes")
operator fun Pair<Double, Double>.times(n: Number) = map { it * n.toDouble() }

@JvmName("floatPairTimes")
operator fun Pair<Float, Float>.times(n: Number) = map { it * n.toFloat() }

@JvmName("intPairTimes")
operator fun Pair<Int, Int>.times(n: Int) = map { it * n }

@JvmName("intPairTimes")
operator fun Pair<Int, Int>.times(n: Double): Pair<Double, Double> = map { it * n }

@JvmName("intPairTimes")
operator fun Pair<Int, Int>.times(n: Float): Pair<Float, Float> = map { it * n }


/* Minus section */
@JvmName("doublePairMinus")
operator fun Pair<Double, Double>.minus(n: Number) = map { it - n.toDouble() }

@JvmName("floatPairMinus")
operator fun Pair<Float, Float>.minus(n: Number) = map { it - n.toFloat() }

@JvmName("intPairMinus")
operator fun Pair<Int, Int>.minus(n: Int): Pair<Int, Int> = map { it - n }

@JvmName("intPairMinus")
operator fun Pair<Int, Int>.minus(n: Double): Pair<Double, Double> = map { it - n }

@JvmName("intPairMinus")
operator fun Pair<Int, Int>.minus(n: Float): Pair<Float, Float> = map { it - n }


/* Plus section */
@JvmName("doublePairPlus")
operator fun Pair<Double, Double>.plus(n: Number) = map { it + n.toDouble() }

@JvmName("floatPairPlus")
operator fun Pair<Float, Float>.plus(n: Number) = map { it + n.toFloat() }

@JvmName("intPairPlus")
operator fun Pair<Int, Int>.plus(n: Int): Pair<Int, Int> = map { it + n }

@JvmName("intPairPlus")
operator fun Pair<Int, Int>.plus(n: Double): Pair<Double, Double> = map { it + n }

@JvmName("intPairPlus")
operator fun Pair<Int, Int>.plus(n: Float): Pair<Float, Float> = map { it + n }


/* Div section */
@JvmName("doublePairDiv")
operator fun Pair<Double, Double>.div(n: Number) = map { it / n.toDouble() }

@JvmName("floatPairDiv")
operator fun Pair<Float, Float>.div(n: Number) = map { it / n.toFloat() }

@JvmName("intPairDiv")
operator fun Pair<Int, Int>.div(n: Int): Pair<Int, Int> = map { it / n }

@JvmName("intPairDiv")
operator fun Pair<Int, Int>.div(n: Double): Pair<Double, Double> = map { it / n }

@JvmName("intPairDiv")
operator fun Pair<Int, Int>.div(n: Float): Pair<Float, Float> = map { it / n }
