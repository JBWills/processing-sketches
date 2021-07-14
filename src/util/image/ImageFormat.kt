package util.image

import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC3
import org.bytedeco.opencv.global.opencv_core.CV_8UC4
import org.opencv.core.Mat
import org.opencv.core.Scalar
import processing.core.PConstants
import processing.core.PImage
import util.gray
import util.toARGBScalar
import util.toRGBScalar
import java.awt.Color

enum class ImageFormat(val openCVFormat: Int, val pImageFormat: Int, val numChannels: Int) {
  ARGB(CV_8UC4, PConstants.ARGB, 4),
  RGB(CV_8UC3, PConstants.RGB, 4),
  Gray(CV_8UC1, PConstants.GRAY, 1),
  Alpha(CV_8UC1, PConstants.ALPHA, 1),
  ;

  fun colorToScalar(c: Color): Scalar = when (this) {
    ARGB -> c.toARGBScalar()
    RGB -> c.toRGBScalar()
    Gray -> Scalar(c.gray)
    Alpha -> Scalar(c.alpha.toDouble())
  }

  fun toIntValue(arr: ByteArray): Int {
    val ints = arr.map { it.toInt() }
    return when (this) {
      ARGB -> Color(ints[1], ints[2], ints[3], ints[0]).rgb
      RGB -> Color(ints[0], ints[1], ints[2]).rgb
      Gray,
      Alpha -> ints[0]
    }
  }

  companion object {
    fun Mat.getFormat() = when (type()) {
      CV_8UC4 -> ARGB
      CV_8UC3 -> RGB
      CV_8UC1 -> Gray
      else -> Gray
    }

    fun PImage.getFormat() = when (format) {
      PConstants.ARGB -> ARGB
      PConstants.RGB -> RGB
      PConstants.GRAY -> Gray
      PConstants.ALPHA -> Alpha
      else -> Alpha
    }

    fun fromOpenCVInt(i: Int): ImageFormat? = when (i) {
      CV_8UC4 -> ARGB
      CV_8UC3 -> RGB
      CV_8UC1 -> Gray
      else -> null
    }

    fun fromProcessingInt(i: Int): ImageFormat? = when (i) {
      PConstants.ARGB -> ARGB
      PConstants.RGB -> RGB
      PConstants.GRAY -> Gray
      PConstants.ALPHA -> Alpha
      else -> null
    }
  }
}
