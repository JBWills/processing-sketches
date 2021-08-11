package util.image.opencvMat

import coordinate.Point
import coordinate.Segment
import geomerativefork.src.util.chunkFilterInterpolated
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.DoubleRange
import util.doIf
import util.polylines.PolyLine
import util.polylines.closed
import util.polylines.toSegment

private fun Point.inMask(mask: Mat, thresholdValueRange: DoubleRange, inverted: Boolean): Boolean {
  val pixelValue = mask.get(this) ?: return false
  val inMask = thresholdValueRange.contains(pixelValue)
  return if (inverted) !inMask else inMask
}

/**
 * Get the masked polylines based on the grayscale image.
 *
 * @param mask the image to use as a mask
 * @param inverted if true, only the lines that exist ABOVE the threshold will be returned,
 *   if false, only lines below the threshold are returned.
 * @return the masked lines.
 */
@JvmName("maskedByImagePolyLineList")
fun List<PolyLine>.maskedByImage(
  mask: Mat,
  inverted: Boolean = false,
  thresholdValueRange: DoubleRange = 128.0..Double.MAX_VALUE
): List<List<PolyLine>> =
  map { contour -> contour.maskedByImage(mask, inverted, thresholdValueRange) }

fun PolyLine.maskedByImage(
  mask: Mat,
  inverted: Boolean = false,
  thresholdValueRange: DoubleRange = 128.0..Double.MAX_VALUE,
): List<PolyLine> {
  val inMaskPredicate = { p: Point -> p.inMask(mask, thresholdValueRange, inverted) }
  return chunkFilterInterpolated(
    predicate = inMaskPredicate,
    getBoundaryValue = { (currPoint, _, _), (nextPoint, _, _) ->
      Segment(currPoint, nextPoint)
        .binarySearchForBoundary(inMaskPredicate)
    },
  )
}

fun Segment.maskedByImage(
  mask: Mat,
  inverted: Boolean = false,
  thresholdValueRange: DoubleRange = 128.0..Double.MAX_VALUE,
  checkEveryXPixels: Int = 5,
): List<Segment> =
  split(length / checkEveryXPixels)
    .maskedByImage(mask, inverted, thresholdValueRange)
    .filterNot { it.isEmpty() }
    .map { it.toSegment() }

@JvmName("fillPolyMulti")
fun Mat.fillPoly(polys: List<PolyLine>, color: Scalar = Scalar(255.0)) =
  Imgproc.fillPoly(this, polys.toMatOfPointList(), color)

@JvmName("fillPolyMultiMulti")
fun Mat.fillPoly(polys: List<List<PolyLine>>, color: Scalar = Scalar(255.0)) =
  fillPoly(polys.flatten(), color)

fun Mat.fillPoly(poly: PolyLine, color: Scalar = Scalar(255.0)) = fillPoly(listOf(poly), color)

fun Array<org.opencv.core.Point>.toPolyLine(close: Boolean = true): PolyLine =
  map { Point(it.x, it.y) }
    .doIf(close) { it.closed() }

fun MatOfPoint.toPolyLine(close: Boolean = false): PolyLine =
  toArray().toPolyLine(close)

fun MatOfPoint2f.toPolyLine(close: Boolean = false): PolyLine =
  toArray().toPolyLine(close)

fun List<PolyLine>.toMatOfPointList(): List<MatOfPoint> = map { it.toMatOfPoint() }
fun List<PolyLine>.toMatOfPoint2fList(): List<MatOfPoint2f> = map { it.toMatOfPoint2f() }

fun PolyLine.toMatOfPoint(): MatOfPoint =
  MatOfPoint().also { it.fromList(map(Point::toOpenCvPoint)) }

fun PolyLine.toMatOfPoint2f(): MatOfPoint2f =
  MatOfPoint2f().also { it.fromList(map(Point::toOpenCvPoint)) }

/**
 * Use OpenCV simplification algorithm
 *
 * @param epsilon max distance between the length of the approx curve and the real curve. Higher
 *   means smoother, simpler line that may be very different from the original
 * @return the new curve
 */
fun MatOfPoint2f.simplify(epsilon: Double): MatOfPoint2f =
  applyWithDestPoints { src, dest -> Imgproc.approxPolyDP(src, dest, epsilon, false) }
