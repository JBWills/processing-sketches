package util.image.opencvContouring

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import geomerativefork.src.util.chunkFilter
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import util.debugLog
import util.image.ChannelDepth
import util.image.ContourApproximationModes.TC89KCOS
import util.image.ContourRetrievalModes.ListMode
import util.image.ImageFormat.Gray
import util.image.bounds
import util.image.cloneEmpty
import util.image.copy
import util.image.getOr
import util.image.minMax
import util.io.geoJson.loadGeoMatMemo
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.bounds
import util.pointsAndLines.polyLine.toPolyLine
import kotlin.math.max


const val CannyRatio = 3

fun Mat.findRawContours(): List<MatOfPoint> = arrayListOf<MatOfPoint>()
  .also { contours ->
    Imgproc.findContours(this, contours, cloneEmpty(), ListMode.typeVal, TC89KCOS.typeVal)
  }

fun Mat.findContours(): List<PolyLine> =
  findRawContours()
    .map { it.toPolyLine() }
    .filter { it.size > 2 }

fun Mat.geoTiffToGray(): Mat = applyWithDest(Gray) { src, dest ->
  val (min, max) = src.minMax
  val actualMin = max(0.0, min)

  debugLog(actualMin, max)

  copy()
    .subtract(actualMin, false)
    .divide(max - actualMin, false)
    .convertTo(ChannelDepth.CV_8U, dest, alpha = 255.0)
}

fun List<PolyLine>.maskContours(mask: Mat, inverted: Boolean = false): List<PolyLine> =
  flatMap { contour ->
    contour.chunkFilter {
      val inMask = mask.getOr(it, 0.0) > 128.0
      if (inverted) !inMask else inMask
    }
  }

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
  val contourDatas = thresholdsLargestToSmallest.map { (threshold, offset) ->
    val thresholdMat = mat
      .threshold(threshold)
      .copyTo(emptyMat, offset - combinedBounds.topLeft)

    val thresholdEdges = thresholdMat.findContours()
    val maskedEdges = thresholdEdges.maskContours(rollingUnionMat, inverted = true)

    rollingUnionMat = rollingUnionMat.bitwiseOr(thresholdMat)

    ContourData(
      threshold,
      rollingUnionMat,
      maskedEdges,
    )
  }

  return MatContourResponse(
    mat,
    mat.bounds - combinedBounds.topLeft,
    contourDatas.sortedBy { it.threshold },
  )
}

val contourMemo = (::loadAndContourWithOffset).memoize()
