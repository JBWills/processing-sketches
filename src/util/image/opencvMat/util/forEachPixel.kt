package util.image.opencvMat.util

import org.opencv.core.Mat
import util.image.opencvMat.toDoubleArray
import util.image.opencvMat.toIntArray

fun Mat.forEachIntRow(block: (Int, IntArray) -> Unit) {
  toIntArray(0).forEachIndexed(block)
}

fun Mat.forEachDoubleRow(block: (Int, DoubleArray) -> Unit) {
  toDoubleArray(0).forEachIndexed(block)
}
