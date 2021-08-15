package util.image.opencvMat

import geomerativefork.src.util.bound
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import util.image.opencvMat.BorderType.BorderReflect

val DilationKernel: Mat = Imgproc.getStructuringElement(MorphShapes.Ellipse.type, Size(5.0, 5.0))

val DilationAnchor = Point(-1.0, -1.0)

fun Mat.dilateOrErode(
  f: (Mat, Mat, Mat, Point, Int, Int, Scalar) -> Unit,
  amountPx: Double,
  inPlace: Boolean = false,
  dilationKernel: Mat = DilationKernel,
  anchor: coordinate.Point? = null,
  borderType: BorderType = BorderReflect,
): Mat = applyWithDest(inPlace = inPlace) { src, dest ->
  val iterations = (amountPx / 2.0).toInt().bound(start = 1, end = Int.MAX_VALUE)
  val anchorPoint = anchor?.toOpenCvPoint() ?: DilationAnchor
  f(src, dest, dilationKernel, anchorPoint, iterations, borderType.type, Scalar(0.0))
}

fun Mat.dilate(
  amountPx: Double,
  inPlace: Boolean = false,
  dilationKernel: Mat = DilationKernel,
  anchor: coordinate.Point? = null,
  borderType: BorderType = BorderReflect,
) = dilateOrErode(Imgproc::dilate, amountPx, inPlace, dilationKernel, anchor, borderType)

fun Mat.erode(
  amountPx: Double,
  inPlace: Boolean = false,
  dilationKernel: Mat = DilationKernel,
  anchor: coordinate.Point? = null,
  borderType: BorderType = BorderReflect,
) = dilateOrErode(Imgproc::erode, amountPx, inPlace, dilationKernel, anchor, borderType)
