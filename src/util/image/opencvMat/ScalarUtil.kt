package util.image.opencvMat

import org.opencv.core.Scalar
import util.iterators.mapArray

val Scalar.values: DoubleArray get() = `val`

val Scalar.first: Double get() = values[0]

fun Scalar.toFloatArray() = values.mapArray { it.toFloat() }.toFloatArray()

fun DoubleArray.toScalar() = Scalar(this)
