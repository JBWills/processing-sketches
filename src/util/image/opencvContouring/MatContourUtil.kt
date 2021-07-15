package util.image.opencvContouring

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import util.image.ContourApproximationModes.Simple
import util.image.ContourRetrievalModes.ListMode
import util.image.ImageFormat.Gray
import util.image.bounds
import util.image.cloneEmpty
import util.io.geoJson.loadGeoMatMemo
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.bounds
import util.pointsAndLines.polyLine.toPolyLine


const val CannyRatio = 3

fun Mat.findContours(): List<PolyLine> =
  arrayListOf<MatOfPoint>()
    .also { contours ->
      Imgproc.findContours(this, contours, cloneEmpty(), ListMode.typeVal, Simple.typeVal)
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
): MatContourResponse =
  loadAndContourWithOffset(filename, thresholds.associateWith { Point.Zero })

data class ContourData(
  val threshold: Double,
  val binaryImage: Mat,
  val contours: List<PolyLine>
)

data class MatContourResponse(
  val baseMat: Mat,
  val baseMatBoundsInUnionRect: BoundRect,
  val contours: List<ContourData>,
)

fun loadAndContourWithOffset(
  filename: String,
  thresholdsToOffset: Map<Double, Point>
): MatContourResponse {
  val mat = loadGeoMatMemo(filename)
  val (matTopLeft, matBottomRight) = mat.bounds.minMax
  val (minOffset, maxOffset) = thresholdsToOffset.values.bounds.minMax

  val newTopLeft = matTopLeft + Point.minXY(minOffset, Point.Zero)
  val newBottomRight = matBottomRight + Point.maxXY(maxOffset, Point.Zero)
  val combinedBounds = mat.bounds
    .expandToInclude(newTopLeft)
    .expandToInclude(newBottomRight)

  val emptyMat =
    Mat.zeros(combinedBounds.height.toInt(), combinedBounds.width.toInt(), Gray.openCVFormat)
  var rollingUnionMat =
    Mat.zeros(combinedBounds.height.toInt(), combinedBounds.width.toInt(), Gray.openCVFormat)

  val thresholdsLargestToSmallest = thresholdsToOffset.entries.sortedBy { it.key }.reversed()
  val thresholdToUnionMats = thresholdsLargestToSmallest.associate { (threshold, offset) ->
    val thresholdMat = mat
      .threshold(threshold)
      .copyTo(emptyMat, offset - combinedBounds.topLeft)
    rollingUnionMat = rollingUnionMat.bitwiseOr(thresholdMat)
    threshold to (thresholdMat.findContours() to rollingUnionMat)
  }

  return MatContourResponse(
    mat,
    mat.bounds - combinedBounds.topLeft,
    thresholdToUnionMats.map { (threshold, contoursToMat) ->
      ContourData(threshold, contoursToMat.second, contoursToMat.first)
    }.sortedBy { it.threshold },
  )
}

val contourMemo = (::loadAndContourWithOffset).memoize()
