package util.image

import org.bytedeco.opencv.global.opencv_core.CV_32FC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_core.CV_8UC4
import org.opencv.core.Mat
import org.opencv.core.Scalar
import processing.core.PConstants
import processing.core.PImage
import util.base.gray
import util.base.toArgbScalar
import util.base.toBgrScalar
import util.base.toBgraScalar
import util.base.toRgbScalar
import util.base.toRgbaScalar
import util.image.opencvMat.values
import java.awt.Color

enum class ImageFormat(val openCVFormat: Int, val pImageFormat: Int, val numChannels: Int) {
  // Rgba is used by OpenCV but not by Processing
  RgbaOpenCV(CV_8UC4, PConstants.ARGB, 4),

  // Argb is used by Processing but not by OpenCV
  ArgbProcessing(CV_8UC4, PConstants.ARGB, 4),
  Rgb(CV_8UC3, PConstants.RGB, 3),
  Bgr(CV_8UC3, PConstants.RGB, 3),
  Bgra(CV_8UC4, PConstants.ARGB, 4),
  Gray(CV_8UC1, PConstants.GRAY, 1),
  Alpha(CV_8UC1, PConstants.ALPHA, 1),
  Float32(CV_32FC1, PConstants.ALPHA, 1),
  ;

  @Suppress("MemberVisibilityCanBePrivate")
  fun getRed(arr: List<Number>): Int = when (this) {
    Alpha -> 255
    ArgbProcessing -> arr[1]
    RgbaOpenCV, Rgb, Gray, Float32 -> arr[0]
    Bgr, Bgra -> arr[2]
  }.toInt()

  @Suppress("MemberVisibilityCanBePrivate")
  fun getGreen(arr: List<Number>): Int = when (this) {
    Alpha -> 255
    ArgbProcessing -> arr[2]
    Gray, Alpha, Float32 -> arr[0]
    RgbaOpenCV, Rgb, Bgr, Bgra -> arr[1]
  }.toInt()

  @Suppress("MemberVisibilityCanBePrivate")
  fun getBlue(arr: List<Number>): Int = when (this) {
    Alpha -> 255
    ArgbProcessing -> arr[3]
    RgbaOpenCV, Rgb -> arr[2]
    Bgr, Bgra, Gray, Alpha, Float32 -> arr[0]
  }.toInt()

  @Suppress("MemberVisibilityCanBePrivate")
  fun getAlpha(arr: List<Number>): Int = when (this) {
    RgbaOpenCV, Bgra -> arr[3]
    ArgbProcessing -> arr[0]
    Alpha -> arr[0]
    Gray, Float32, Bgr, Rgb -> 255.0
  }.toInt()

  private fun trimScalar(s: Scalar): Scalar = when (numChannels) {
    1 -> Scalar(s.values[0])
    2 -> Scalar(s.values[0], s.values[1])
    3 -> Scalar(s.values[0], s.values[1], s.values[2])
    4 -> Scalar(s.values[0], s.values[1], s.values[2], s.values[3])
    else -> throw Exception("Unsupported number of channels provided: $numChannels")
  }

  fun doubleToScalar(d: Double, alpha: Double = 255.0): Scalar = trimScalar(Scalar(d, d, d, alpha))

  fun colorToScalar(c: Color): Scalar = when (this) {
    ArgbProcessing -> c.toArgbScalar()
    RgbaOpenCV -> c.toRgbaScalar()
    Rgb -> c.toRgbScalar()
    Bgra -> c.toBgraScalar()
    Bgr -> c.toBgrScalar()
    Gray -> Scalar(c.gray)
    Alpha -> Scalar(c.alpha.toDouble())
    Float32 -> Scalar(c.gray)
  }

  fun toRgbInt(arr: ByteArray): Int {
    val doubleArr = arr.map { it.toInt().toDouble() }.toDoubleArray()
    return toColor(doubleArr).rgb
  }

  fun toColor(arr: DoubleArray): Color {
    val intArr = arr.map { it.toInt() }
    return Color(getRed(intArr), getGreen(intArr), getBlue(intArr), getAlpha(intArr))
  }

  fun toColor(arr: Array<Int>): Color {
    val intList = arr.toList()
    return Color(getRed(intList), getGreen(intList), getBlue(intList), getAlpha(intList))
  }

  companion object {
    fun Mat.getFormat(): ImageFormat = fromOpenCVInt(type())

    fun PImage.getFormat() = when (format) {
      PConstants.ARGB -> RgbaOpenCV
      PConstants.RGB -> Rgb
      PConstants.GRAY -> Gray
      PConstants.ALPHA -> Alpha
      else -> Alpha
    }

    fun fromOpenCVInt(i: Int): ImageFormat = when (i) {
      CV_8UC4 -> RgbaOpenCV
      CV_8UC3 -> Rgb
      CV_8UC1 -> Gray
      CV_32FC1 -> Float32
      else -> Gray
    }

    fun fromProcessingInt(i: Int): ImageFormat? = when (i) {
      PConstants.ARGB -> RgbaOpenCV
      PConstants.RGB -> Rgb
      PConstants.GRAY -> Gray
      PConstants.ALPHA -> Alpha
      else -> null
    }
  }
}
