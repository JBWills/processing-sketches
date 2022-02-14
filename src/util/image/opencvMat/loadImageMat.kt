package util.image.opencvMat

import arrow.core.memoize
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import util.image.ImageFormat.Bgr
import util.image.ImageFormat.Bgra
import util.image.ImageFormat.Rgb
import util.image.ImageFormat.RgbaOpenCV
import util.image.converted
import util.image.opencvMat.debug.printDebug
import util.image.opencvMat.flags.ImreadFlags
import java.io.File

/**
 * OpenCV loads images in BGR, we want to use them as RGB though.
 */
private fun Mat.convertFromBgrToRgb(): Mat {
  if (channels() == 4) {
    printDebug("OnloadBefore")
    return converted(from = Bgra, to = RgbaOpenCV).also { it.printDebug("onLoadAfter") }
  } else if (channels() == 3) {
    printDebug("OnloadBefore")
    return converted(from = Bgr, to = Rgb).also { it.printDebug("onLoadAfter") }
  }

  // Don't do anything with grayscale images.
  return this
}

fun loadImageMat(path: String, flags: ImreadFlags = ImreadFlags.ImreadUnchanged): Mat? =
  if (File(path).exists()) Imgcodecs.imread(path, flags.value).convertFromBgrToRgb() else null

val loadImageMatMemo = { path: String, flags: ImreadFlags -> loadImageMat(path, flags) }.memoize()
