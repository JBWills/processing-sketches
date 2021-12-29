package util.image.opencvMat

import org.opencv.core.Mat

val Mat.width get() = cols()
val Mat.height get() = rows()

fun Mat.getNewWidth(newHeight: Number) =
  newHeight.toDouble() * (width.toDouble() / height.toDouble())

fun Mat.getNewHeight(newWidth: Number) =
  newWidth.toDouble() * (height.toDouble() / width.toDouble())
