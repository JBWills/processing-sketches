package util.image

import org.bytedeco.opencv.global.opencv_core
import org.opencv.core.Mat
import processing.core.PImage
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Companion.getFormat
import java.nio.ByteBuffer

fun Mat.asIntBuffer(fromFormat: ImageFormat = getFormat()) =
  converted(fromFormat, ARGB)
    .getByteArray()
    .asIntBuffer()

fun Mat.copyTo(arr: IntArray) {
  asIntBuffer().copyTo(arr)
}

fun Mat.copyTo(pImage: PImage) {
  copyTo(pImage.pixels)
  pImage.updatePixels()
}

fun Mat.toPImage(): PImage {
  val format = getFormat()
  return PImage(width(), height(), format.pImageFormat).also { copyTo(it) }
}

fun Mat.toEmptyPImage(format: ImageFormat = getFormat()): PImage =
  PImage(cols(), rows(), format.pImageFormat)

fun PImage.toEmptyMat(format: ImageFormat = getFormat()): Mat =
  Mat(height, width, format.openCVFormat)

fun PImage.toEmptyOpenCVMat(): Mat = Mat(height, width, opencv_core.CV_8UC4)

fun PImage.toOpenCVRGB(): Mat = Mat(height, width, opencv_core.CV_8UC3)
  .apply { put(0, 0, pixels) }

fun ByteArray.toMat(width: Int, height: Int, format: ImageFormat): Mat =
  Mat(height, width, format.openCVFormat)
    .also { mat -> mat.put(0, 0, this) }

fun Mat.toByteArray(): ByteArray = getByteArray()

fun PImage.toMat(): Mat {
  loadPixels()
  val bArray = ByteArray(pixels.size * 4)
  ByteBuffer.allocate(pixels.size * 4).apply {
    asIntBuffer().put(pixels)
    get(bArray)
  }

  return toEmptyMat(format = ARGB).apply { put(0, 0, bArray) }
}
