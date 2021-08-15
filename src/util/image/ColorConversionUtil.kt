package util.image

import org.bytedeco.opencv.global.opencv_imgproc.COLOR_RGBA2GRAY
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import processing.core.PImage
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Alpha
import util.image.ImageFormat.Float32
import util.image.ImageFormat.Gray
import util.image.ImageFormat.RGB
import util.image.opencvMat.asBlankMat
import util.image.opencvMat.createMat
import util.image.opencvMat.geoTiffToGray
import util.image.opencvMat.merge
import util.image.opencvMat.split
import util.image.opencvMat.toPImage
import java.awt.Color

fun Mat.toArgbPImage(from: ImageFormat): PImage = converted(from, ARGB).toPImage()

fun Mat.converted(from: ImageFormat, to: ImageFormat): Mat {
  if (from == to) return this

  val splitMat: List<Mat> = split()
  val white: Mat = createMat(rows(), cols(), Gray, Color.WHITE)
  val argbMat: List<Mat> = when (from) {
    ARGB -> splitMat
    RGB -> listOf(white) + splitMat
    Gray -> listOf(white, splitMat[0], splitMat[0], splitMat[0])
    Float32 -> geoTiffToGray().let { listOf(white, it, it, it) }
    Alpha -> splitMat + listOf(white, white, white)
  }

  val alphaMat = argbMat[0]
  val rgbMat = argbMat.slice(1..3)

  return when (to) {
    ARGB -> argbMat.merge(ARGB)
    RGB -> rgbMat.merge(RGB)
    Gray -> asBlankMat(Gray.openCVFormat).also {
      Imgproc.cvtColor((rgbMat + alphaMat).merge(ARGB), it, COLOR_RGBA2GRAY)
    }
    Float32 -> asBlankMat(Float32.openCVFormat).also {
      Imgproc.cvtColor((rgbMat + alphaMat).merge(ARGB), it, COLOR_RGBA2GRAY)
    }
    Alpha -> alphaMat
  }
}
