package util.image.opencvMat

import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.Float32
import util.image.opencvMat.PointPolyResult.Inside
import util.image.opencvMat.PointPolyResult.OnEdge
import util.image.opencvMat.PointPolyResult.Outside
import util.image.opencvMat.matOfPoint.toMatOfPoint2f
import util.image.opencvMat.matOfPoint.toMatOfPoint2fList
import util.polylines.PolyLine
import util.polylines.length

enum class PointPolyResult {
  Inside,
  Outside,
  OnEdge,
}

fun MatOfPoint2f.inPointPoly(p: Point): PointPolyResult = pointPolyTest(p, false).let {
  when {
    it == 0.0 -> OnEdge
    it > 0 -> Inside
    else -> Outside
  }
}

fun MatOfPoint2f.pointPolyTest(p: Point, measureDist: Boolean = true): Double =
  Imgproc.pointPolygonTest(this, p.toOpenCvPoint(), measureDist)

@JvmName("pointPolyTestMatOfPoint2fList")
fun List<MatOfPoint2f>.pointPolyTest(m: Mat): Mat {
  val mat = m.cloneEmpty(format = Float32)

  mat.bounds.mapGrid { p ->
    val allResults = map { it.pointPolyTest(p) }
    val (allResultsPositiveOrZero, allResultsNegative) = allResults.partition { it >= 0 }

    val result =
      allResultsPositiveOrZero.minOrNull() ?: allResultsNegative.maxOrNull() ?: Double.MIN_VALUE
    mat.put(p, result)
  }

  return mat
}

@JvmName("MatOfPoint2fPointPolyTest")
fun MatOfPoint2f.pointPolyTest(m: Mat): Mat =
  listOf(this).pointPolyTest(m)

fun PolyLine.pointPolyTest(m: Mat): Mat =
  toMatOfPoint2f().pointPolyTest(m)

@JvmName("pointPolyTestPolyLineList")
fun List<PolyLine>.pointPolyTest(m: Mat, minPolyLength: Double = 100.0): Mat =
  filter { line -> line.length > minPolyLength }
    .toMatOfPoint2fList()
    .pointPolyTest(m)

@JvmName("pointPolyTestMatOfPointList")
fun List<MatOfPoint>.pointPolyTest(m: Mat): Mat =
  toMatOfPoint2fList().pointPolyTest(m)

fun MatOfPoint.pointPolyTest(m: Mat): Mat =
  toMatOfPoint2f().pointPolyTest(m)
