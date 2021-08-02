package util.image.opencvMat

import coordinate.Point
import geomerativefork.src.util.chunkFilter
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.polylines.closed
import util.polylines.polyLine.PolyLine

/**
 * Get the masked polylines based on the grayscale image.
 *
 * @param mask the image to use as a mask
 * @param inverted if true, only the lines that exist ABOVE the threshold will be returned,
 *   if false, only lines below the threshold are returned.
 * @return the masked lines.
 */
fun List<PolyLine>.maskedByImage(
  mask: Mat,
  inverted: Boolean = false,
  threshold: Double = 128.0
): List<PolyLine> = flatMap { contour ->
  contour.chunkFilter {
    val inMask = mask.getOr(it, 0.0) > threshold
    if (inverted) !inMask else inMask
  }
}

@JvmName("fillPolyMulti")
fun Mat.fillPoly(polys: List<PolyLine>, color: Scalar = Scalar(255.0)) =
  Imgproc.fillPoly(this, polys.toMatOfPointList(), color)

@JvmName("fillPolyMultiMulti")
fun Mat.fillPoly(polys: List<List<PolyLine>>, color: Scalar = Scalar(255.0)) =
  fillPoly(polys.flatten(), color)

fun Mat.fillPoly(poly: PolyLine, color: Scalar = Scalar(255.0)) = fillPoly(listOf(poly), color)

fun MatOfPoint.toPolyLine(): PolyLine =
  toArray()
    .map { Point(it.x, it.y) }
    .closed()

fun List<PolyLine>.toMatOfPointList() = map { it.toMatOfPoint() }

fun PolyLine.toMatOfPoint(): MatOfPoint =
  MatOfPoint().also { it.fromList(map(Point::toOpenCvPoint)) }
