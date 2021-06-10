package util.iterators

import geomerativefork.src.util.mapArray

/*
 * Times
 */

@JvmName("intListTimes")
operator fun List<Int>.times(amt: Int): List<Int> = map { it.times(amt) }

@JvmName("intListTimesDouble")
operator fun List<Int>.times(amt: Double): List<Double> = map { it.times(amt) }

@JvmName("intListTimesFloat")
operator fun List<Int>.times(amt: Float): List<Float> = map { it.times(amt) }

@JvmName("doubleListTimes")
operator fun List<Double>.times(amt: Number): List<Double> = map { it.times(amt.toDouble()) }

@JvmName("floatListTimes")
operator fun List<Float>.times(amt: Number): List<Float> = map { it.times(amt.toFloat()) }

@JvmName("intArrayTimes")
operator fun Array<Int>.times(amt: Int): Array<Int> = mapArray { it.times(amt) }

@JvmName("intArrayTimesDouble")
operator fun Array<Int>.times(amt: Double): Array<Double> = mapArray { it.times(amt) }

@JvmName("intArrayTimesFloat")
operator fun Array<Int>.times(amt: Float): Array<Float> = mapArray { it.times(amt) }

@JvmName("doubleArrayTimes")
operator fun Array<Double>.times(amt: Number): Array<Double> = mapArray { it.times(amt.toDouble()) }

@JvmName("floatArrayTimes")
operator fun Array<Float>.times(amt: Number): Array<Float> = mapArray { it.times(amt.toFloat()) }

/*
 * Plus
 */

@JvmName("intListPlus")
operator fun List<Int>.plus(amt: Int): List<Int> = map { it.plus(amt) }

@JvmName("intListPlusDouble")
operator fun List<Int>.plus(amt: Double): List<Double> = map { it.plus(amt) }

@JvmName("intListPlusFloat")
operator fun List<Int>.plus(amt: Float): List<Float> = map { it.plus(amt) }

@JvmName("doubleListPlus")
operator fun List<Double>.plus(amt: Number): List<Double> = map { it.plus(amt.toDouble()) }

@JvmName("floatListPlus")
operator fun List<Float>.plus(amt: Number): List<Float> = map { it.plus(amt.toFloat()) }

@JvmName("intArrayPlus")
operator fun Array<Int>.plus(amt: Int): Array<Int> = mapArray { it.plus(amt) }

@JvmName("intArrayPlusDouble")
operator fun Array<Int>.plus(amt: Double): Array<Double> = mapArray { it.plus(amt) }

@JvmName("intArrayPlusFloat")
operator fun Array<Int>.plus(amt: Float): Array<Float> = mapArray { it.plus(amt) }

@JvmName("doubleArrayPlus")
operator fun Array<Double>.plus(amt: Number): Array<Double> = mapArray { it.plus(amt.toDouble()) }

@JvmName("floatArrayPlus")
operator fun Array<Float>.plus(amt: Number): Array<Float> = mapArray { it.plus(amt.toFloat()) }

/*
 * Minus
 */

@JvmName("intListMinus")
operator fun List<Int>.minus(amt: Int): List<Int> = map { it.minus(amt) }

@JvmName("intListMinusDouble")
operator fun List<Int>.minus(amt: Double): List<Double> = map { it.minus(amt) }

@JvmName("intListMinusFloat")
operator fun List<Int>.minus(amt: Float): List<Float> = map { it.minus(amt) }

@JvmName("doubleListMinus")
operator fun List<Double>.minus(amt: Number): List<Double> = map { it.minus(amt.toDouble()) }

@JvmName("floatListMinus")
operator fun List<Float>.minus(amt: Number): List<Float> = map { it.minus(amt.toFloat()) }

@JvmName("intArrayMinus")
operator fun Array<Int>.minus(amt: Int): Array<Int> = mapArray { it.minus(amt) }

@JvmName("intArrayMinusDouble")
operator fun Array<Int>.minus(amt: Double): Array<Double> = mapArray { it.minus(amt) }

@JvmName("intArrayMinusFloat")
operator fun Array<Int>.minus(amt: Float): Array<Float> = mapArray { it.minus(amt) }

@JvmName("doubleArrayMinus")
operator fun Array<Double>.minus(amt: Number): Array<Double> = mapArray { it.minus(amt.toDouble()) }

@JvmName("floatArrayMinus")
operator fun Array<Float>.minus(amt: Number): Array<Float> = mapArray { it.minus(amt.toFloat()) }

/*
 * Div
 */

@JvmName("intListDiv")
operator fun List<Int>.div(amt: Int): List<Int> = map { it.div(amt) }

@JvmName("intListDivDouble")
operator fun List<Int>.div(amt: Double): List<Double> = map { it.div(amt) }

@JvmName("intListDivFloat")
operator fun List<Int>.div(amt: Float): List<Float> = map { it.div(amt) }

@JvmName("doubleListDiv")
operator fun List<Double>.div(amt: Number): List<Double> = map { it.div(amt.toDouble()) }

@JvmName("floatListDiv")
operator fun List<Float>.div(amt: Number): List<Float> = map { it.div(amt.toFloat()) }

@JvmName("intArrayDiv")
operator fun Array<Int>.div(amt: Int): Array<Int> = mapArray { it.div(amt) }

@JvmName("intArrayDivDouble")
operator fun Array<Int>.div(amt: Double): Array<Double> = mapArray { it.div(amt) }

@JvmName("intArrayDivFloat")
operator fun Array<Int>.div(amt: Float): Array<Float> = mapArray { it.div(amt) }

@JvmName("doubleArrayDiv")
operator fun Array<Double>.div(amt: Number): Array<Double> = mapArray { it.div(amt.toDouble()) }

@JvmName("floatArrayDiv")
operator fun Array<Float>.div(amt: Number): Array<Float> = mapArray { it.div(amt.toFloat()) }
