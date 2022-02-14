package util.image

import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGBA2GRAY
import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGBA2RGB
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import util.debugLog
import util.image.ImageFormat.Alpha
import util.image.ImageFormat.ArgbProcessing
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Float32
import util.image.ImageFormat.Gray
import util.image.ImageFormat.Rgb
import util.image.ImageFormat.RgbaOpenCV
import util.image.opencvMat.asBlankMat
import util.image.opencvMat.createMat
import util.image.opencvMat.enum.CConv
import util.image.opencvMat.enum.CConv.RgbaToArgb
import util.image.opencvMat.geoTiffToGray
import util.image.opencvMat.merge
import util.image.opencvMat.split
import util.image.opencvMat.split4
import java.awt.Color

fun Mat.converted(from: ImageFormat = getFormat(), to: ImageFormat = getFormat()): Mat {
  val conv = CConv.getCConv(from, to)
  debugLog("Doing $conv conversion: $from -> $to")

  if (conv.isIdentity) return this

  if (conv == RgbaToArgb) {
    debugLog(channels(), type())
    val (r, g, b, a) = split4() ?: return this

    return listOf(a, r, g, b).merge(ArgbProcessing)
  }

  if (conv.value == null) {
    throw Exception("Attempting to use invalid conversion type: $conv")
  }

  val newMat = asBlankMat(to.openCVFormat)
  Imgproc.cvtColor(this, newMat, conv.value)

  return newMat

  val splitMat: List<Mat> = split()
  if (splitMat.isEmpty()) return this

  val white: Mat = createMat(rows(), cols(), Gray, Color.WHITE)
  val rgbaMat: List<Mat> = when (from) {
    RgbaOpenCV -> splitMat
    Rgb -> splitMat + listOf(white)
    Gray -> listOf(splitMat[0], splitMat[0], splitMat[0], white)
    Float32 -> geoTiffToGray().let { listOf(it, it, it, white) }
    Alpha -> listOf(white, white, white) + splitMat
  }

  val alphaMat = rgbaMat[3]

  return when (to) {
    RgbaOpenCV -> rgbaMat.merge(RgbaOpenCV)
    Rgb -> asBlankMat(Rgb.openCVFormat).also {
      Imgproc.cvtColor(rgbaMat.merge(RgbaOpenCV), it, COLOR_RGBA2RGB)
    }
    Gray -> asBlankMat(Gray.openCVFormat).also {
      Imgproc.cvtColor(rgbaMat.merge(RgbaOpenCV), it, COLOR_RGBA2GRAY)
    }
    Float32 -> asBlankMat(Float32.openCVFormat).also {
      Imgproc.cvtColor(rgbaMat.merge(RgbaOpenCV), it, COLOR_RGBA2GRAY)
    }
    Alpha -> alphaMat
  }
}
