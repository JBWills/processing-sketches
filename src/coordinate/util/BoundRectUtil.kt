package coordinate.util

import coordinate.BoundRect

fun BoundRect.scaledToFit(other: BoundRect): BoundRect {
  val sizeRatio = size / other.size
  val tallerAspectRatioThanContainer = sizeRatio.y >= sizeRatio.x

  val scaled =
    if (tallerAspectRatioThanContainer) scaleByHeight(other.height.toInt())
    else scaleByWidth(other.width.toInt())


  return scaled.recentered(other.center)
}
