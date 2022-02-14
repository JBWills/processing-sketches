package util.image.opencvMat.channels

import org.opencv.core.Mat
import util.image.ImageFormat
import util.image.ImageFormat.Alpha
import util.image.ImageFormat.ArgbProcessing
import util.image.ImageFormat.Bgr
import util.image.ImageFormat.Bgra
import util.image.ImageFormat.Float32
import util.image.ImageFormat.Gray
import util.image.ImageFormat.Rgb
import util.image.ImageFormat.RgbaOpenCV
import util.image.opencvMat.split

fun Mat.getAlphaChannel(format: ImageFormat): Mat? {
  val splitMat = split()

  if (splitMat.isEmpty()) {
    return null
  }

  return when (format) {
    Bgra,
    RgbaOpenCV -> splitMat[3]
    ArgbProcessing -> splitMat[0]
    Rgb,
    Bgr,
    Gray,
    Float32 -> null
    Alpha -> splitMat[0]
  }
}

fun Mat.getRedChannel(format: ImageFormat): Mat? {
  val splitMat = split()

  if (splitMat.isEmpty()) {
    return null
  }

  return when (format) {
    Bgra -> splitMat[2]
    RgbaOpenCV -> splitMat[0]
    ArgbProcessing -> splitMat[1]
    Rgb -> splitMat[0]
    Bgr -> splitMat[2]
    Gray -> splitMat[0]
    Float32 -> splitMat[0]
    Alpha -> null
  }
}

fun Mat.getGreenChannel(format: ImageFormat): Mat? {
  val splitMat = split()

  if (splitMat.isEmpty()) {
    return null
  }

  return when (format) {
    Bgra -> splitMat[1]
    RgbaOpenCV -> splitMat[1]
    ArgbProcessing -> splitMat[2]
    Rgb -> splitMat[1]
    Bgr -> splitMat[1]
    Gray -> splitMat[0]
    Float32 -> splitMat[0]
    Alpha -> null
  }
}

fun Mat.getBlueChannel(format: ImageFormat): Mat? {
  val splitMat = split()

  if (splitMat.isEmpty()) {
    return null
  }

  return when (format) {
    Bgra -> splitMat[0]
    RgbaOpenCV -> splitMat[2]
    ArgbProcessing -> splitMat[3]
    Rgb -> splitMat[2]
    Bgr -> splitMat[0]
    Gray -> splitMat[0]
    Float32 -> splitMat[0]
    Alpha -> null
  }
}
