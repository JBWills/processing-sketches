package util.image.opencvContouring

import arrow.core.memoize
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import util.image.ChannelDepth
import util.image.ContourApproximationModes.Tc89L1
import util.image.ContourRetrievalModes.ListMode
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.OpenCVThresholdType
import util.image.OpenCVThresholdType.ThreshBinary
import util.image.cloneEmpty
import util.io.geoJson.loadGeoMatMemo
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.toPolyLine


const val CannyRatio = 3

fun Mat.applyWithDest(
  format: ImageFormat = getFormat(),
  block: (src: Mat, dest: Mat) -> Unit,
): Mat = cloneEmpty(format).also { dest -> block(this, dest) }

fun Mat.canny(threshold: Double): Mat = applyWithDest(Gray) { src, dest ->
  Imgproc.Canny(src, dest, threshold, threshold * CannyRatio, 3, true)
}

fun Mat.threshold(value: Double, type: OpenCVThresholdType = ThreshBinary): Mat =
  applyWithDest(Gray) { src, dest ->
    Imgproc.threshold(src, dest, value, 1.0, type.typeVal)
    dest.convertTo(dest, ChannelDepth.CV_8U.typeVal, 255.0)
  }

fun Mat.findContours(): List<PolyLine> =
  arrayListOf<MatOfPoint>()
    .also { contours ->
      Imgproc.findContours(this, contours, cloneEmpty(), ListMode.typeVal, Tc89L1.typeVal)
    }
    .map { it.toPolyLine() }
    .filter { it.size > 2 }

fun Mat.contour(thresholds: List<Double>): Map<Double, List<PolyLine>> =
  thresholds.associateWith { thresholdValue ->
    threshold(thresholdValue).findContours()
  }

fun loadAndContour(
  filename: String,
  thresholds: List<Double>
): Pair<Mat, Map<Double, List<PolyLine>>> {
  val mat = loadGeoMatMemo(filename)
  val contours = mat.contour(thresholds)
  return mat to contours
}

val contourMemo = (::loadAndContour).memoize()
