package util.image.opencvMat

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.Gray
import util.image.opencvMat.ContourApproximationModes.TC89KCOS
import util.image.opencvMat.ContourRetrievalModes.ListMode
import util.io.geoJson.loadGeoMatMemo
import util.polylines.bounds
import util.polylines.polyLine.PolyLine
import kotlin.math.max


const val CannyRatio = 3

/**
 * Get the contour values as MatOfPoint objects
 */
fun Mat.findRawContours(): List<MatOfPoint> = arrayListOf<MatOfPoint>()
  .also { contours ->
    Imgproc.findContours(this, contours, cloneEmpty(), ListMode.typeVal, TC89KCOS.typeVal)
  }

/**
 * Get contours and process them as PolyLines. Note that this will do some filtering based on the
 * length of the polylines.
 *
 * @return a list of PolyLines
 */
fun Mat.findContours(): List<PolyLine> =
  findRawContours()
    .map { it.toPolyLine() }
    .filter { it.size > 2 }

/**
 * Covert a GeoTiff file to a gray openCV Mat.
 *
 * @return a single channel mat with 8 bit depth
 */
fun Mat.geoTiffToGray(): Mat = applyWithDest(Gray) { src, dest ->
  val (min, max) = src.minMax
  val actualMin = max(0.0, min)

  copy()
    .subtract(actualMin, false)
    .divide(max - actualMin, false)
    .convertTo(ChannelDepth.CV_8U, dest, alpha = 255.0)
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
    val maskedEdges = thresholdEdges.maskedByImage(rollingUnionMat, inverted = true)

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