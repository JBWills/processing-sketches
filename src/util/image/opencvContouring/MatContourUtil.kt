package util.image.opencvContouring

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import util.image.ContourApproximationModes.Tc89L1
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
): MatContourResponse =
  loadAndContourWithOffset(filename, thresholds.associateWith { Point.Zero })

data class ContourData(
  val threshold: Double,
  val binaryImage: Mat,
  val binaryMinusNext: Mat?,
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
  val contourDatas = thresholdsLargestToSmallest.map { (threshold, offset) ->
    val thresholdMat = mat
      .threshold(threshold)
      .copyTo(emptyMat, offset - combinedBounds.topLeft)
    val binaryMinusUnion = thresholdMat.subtract(rollingUnionMat)
    val contours = thresholdMat.findContours()
    rollingUnionMat = rollingUnionMat.bitwiseOr(thresholdMat)

    ContourData(
      threshold,
      rollingUnionMat,
      binaryMinusUnion,
      contours,
    )
  }

  return MatContourResponse(
    mat,
    mat.bounds - combinedBounds.topLeft,
    contourDatas.sortedBy { it.threshold },
  )
}

val contourMemo = (::loadAndContourWithOffset).memoize()
