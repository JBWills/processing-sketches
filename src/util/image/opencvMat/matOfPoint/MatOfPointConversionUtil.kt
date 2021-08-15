package util.image.opencvMat.matOfPoint

import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f

fun MatOfPoint2f.toMatOfPoint() = MatOfPoint(*toArray())
fun MatOfPoint.toMatOfPoint2f() = MatOfPoint2f(*toArray())

fun Iterable<MatOfPoint>.toMatOfPoint2fList() = map { it.toMatOfPoint2f() }
fun Iterable<MatOfPoint2f>.toMatOfPointList() = map { it.toMatOfPoint() }
