package util.image.opencvContouring

import coordinate.Point
import org.opencv.core.Core
import org.opencv.core.Mat
import util.image.bounds

fun Mat.bitwiseOr(other: Mat): Mat = applyWithDest { src, dest ->
  Core.bitwise_or(src, other, dest)
}

fun Mat.bitwiseAnd(other: Mat): Mat = applyWithDest { src, dest ->
  Core.bitwise_and(src, other, dest)
}

fun Mat.bitwiseNot(other: Mat): Mat = applyWithDest { src, dest ->
  Core.bitwise_not(src, other, dest)
}

fun Mat.bitwiseNot(): Mat = applyWithDest { src, dest ->
  Core.bitwise_not(src, dest)
}

fun Mat.bitwiseXor(other: Mat): Mat = applyWithDest { src, dest ->
  Core.bitwise_xor(src, other, dest)
}

fun Mat.copyTo(other: Mat, offset: Point): Mat {
  val offset = offset.map { it.toInt() }
  val newMat = other.clone()

  val matToPasteBounds = bounds

  val boundsOnNewMat = (matToPasteBounds + offset).boundsIntersection(newMat.bounds)

  if (boundsOnNewMat == null || boundsOnNewMat.area == 0.0) return newMat

  val clippedMatToPaste = submat(boundsOnNewMat - offset)

  clippedMatToPaste.copyTo(newMat.submat(boundsOnNewMat))

  return newMat
}
