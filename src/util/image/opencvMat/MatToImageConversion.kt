package util.image.opencvMat

import coordinate.Point
import org.opencv.core.Mat
import processing.core.PImage
import util.image.ImageFormat
import util.image.ImageFormat.ArgbProcessing
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.RgbaOpenCV
import util.image.converted
import java.nio.ByteBuffer


fun Mat.copyTo(pImage: PImage) {
  converted(to = RgbaOpenCV)
    .converted(to = ArgbProcessing)
    .getByteArray()
    .asIntBuffer()
    .get(pImage.pixels)
  pImage.updatePixels()
}

fun Mat.toPImage(): PImage = toEmptyPImage(RgbaOpenCV).also {
//  val pImageData = IntArray(width * height)
//  val matData = converted(to = Rgba).getByteArray()
//  ByteBuffer.wrap(matData).asIntBuffer().get(pImageData)
//  arrayCopy(pImageData, it.pixels)
  copyTo(it)
}

fun Mat.toEmptyPImage(format: ImageFormat = getFormat()): PImage =
  PImage(cols(), rows(), format.pImageFormat)

fun PImage.toEmptyMat(format: ImageFormat = getFormat()): Mat =
  Mat(height, width, format.openCVFormat)

fun ByteArray.toMat(width: Int, height: Int, format: ImageFormat): Mat =
  Mat(height, width, format.openCVFormat)
    .also { mat -> mat.put(0, 0, this) }

/**
 * Paste the contents of a PImage to a mat
 *
 * @param m Mat, must be ARGB and have space for all pixels
 */
fun PImage.copyTo(m: Mat, offset: Point = Point.Zero) {
  loadPixels()
  val bArray = ByteArray(pixels.size * 4)
  ByteBuffer.allocate(pixels.size * 4).apply {
    asIntBuffer().put(pixels)
    get(bArray)
  }

  m.put(bArray, offset)
}

fun PImage.toMat(): Mat = toEmptyMat(format = RgbaOpenCV).also(this::copyTo)
