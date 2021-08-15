package util.image.opencvMat

import org.opencv.core.Scalar
import util.iterators.mapArray

val Scalar.values: DoubleArray get() = `val`

fun Scalar.toFloatArray() = values.mapArray { it.toFloat() }.toFloatArray()
