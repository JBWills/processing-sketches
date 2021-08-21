package util.image.opencvMat

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.Gray
import util.image.opencvMat.ContourApproximationMode.TC89KCOS
import util.image.opencvMat.ContourRetrievalMode.Tree
import util.image.opencvMat.LineFillType.Filled
import util.io.geoJson.loadGeoMatAndBlurMemo
import util.polylines.PolyLine
import util.polylines.bounds
import util.polylines.clipping.simplify.toSimplePolygonsMemo
import util.polylines.simplify
import kotlin.math.max

const val CannyRatio = 3

fun Mat.drawContours(contours: List<MatOfPoint>, fillType: LineFillType = Filled): Mat {
  Imgproc.drawContours(this, contours, -1, Scalar(255.0), fillType.type)

  return this
}

/**
 * Get the contour values as MatOfPoint objects
 */
fun Mat.findRawContours(
  retrievalMode: ContourRetrievalMode = Tree,
  approximationMode: ContourApproximationMode = TC89KCOS
): Pair<List<MatOfPoint>, Mat> =
  Pair(arrayListOf<MatOfPoint>(), cloneEmpty())
    .also { (destContours, destHierarchy) ->
      Imgproc.findContours(
        this,
        destContours,
        destHierarchy,
        retrievalMode.type,
        approximationMode.type,
      )
    }

fun List<MatOfPoint>.toContourPolyLines(lineSimplifyEpsilon: Double): List<PolyLine> = mapNotNull {
  val line = it.toPolyLine(close = true)
  if (line.size > 2) line else null
}.simplify(lineSimplifyEpsilon)
  .toSimplePolygonsMemo()

/**
 * Get contours and process them as PolyLines. Note that this will do some filtering based on the
 * length of the polylines.
 *
 * @return a list of PolyLines
 */
fun Mat.findContours(
  lineSimplifyEpsilon: Double = 0.0,
  retrievalMode: ContourRetrievalMode = Tree,
  approximationMode: ContourApproximationMode = TC89KCOS
): List<PolyLine> =
  findRawContours(retrievalMode, approximationMode)
    .first
    .toContourPolyLines(lineSimplifyEpsilon)

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
  val mat = loadGeoMatAndBlurMemo(filename, 0.0)
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
    val maskedEdges = thresholdEdges.maskedByImage(rollingUnionMat, inverted = true).flatten()

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
