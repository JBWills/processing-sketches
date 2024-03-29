package util.image.opencvMat

import org.opencv.core.Core
import org.opencv.core.Mat
import util.base.DoubleRange
import util.base.letWith
import util.base.toRange
import util.numbers.bound
import util.tuple.map

val Mat.minMax: Pair<Double, Double> get() = Core.minMaxLoc(this).letWith { minVal to maxVal }
val Mat.min: Double get() = minMax.first
val Mat.max: Double get() = minMax.second

fun Mat.getMinMaxValues(
  boundMin: Number = Double.MIN_VALUE,
  boundMax: Number = Double.MAX_VALUE
): DoubleRange = minMax.map { it.bound(boundMin.toDouble()..boundMax.toDouble()) }.toRange()
