package util.image.scaling

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import util.atAmountAlong
import util.image.ImageFormat.Companion.getFormat
import util.image.bounds
import util.image.crop
import util.image.opencvContouring.copyTo
import util.image.resize
import util.image.toEmptyMat

sealed class AnchorType(val percent: Double)
class Start : AnchorType(0.0)
class Center : AnchorType(0.5)
class End : AnchorType(1.0)
class Custom(percent: Double) : AnchorType(percent)


data class Anchor(val horizontal: AnchorType, val vertical: AnchorType) {
  fun getPoint(bound: BoundRect) = Point(
    bound.xRange.atAmountAlong(horizontal.percent),
    bound.yRange.atAmountAlong(vertical.percent),
  )

  fun moveAndScale(from: BoundRect, to: BoundRect, scale: Point = Point(1, 1)): BoundRect {
    val anchorTo = getPoint(to)
    val anchorFrom = getPoint(from)
    return from.translated(anchorTo - anchorFrom).scaled(scale, anchorTo)
  }

  companion object {
    val Center = Anchor(Center(), Center())
    val TopLeft = Anchor(Start(), Start())
    val BottomRight = Anchor(End(), End())
  }
}


data class Crop(val srcFrame: BoundRect, val destFrame: BoundRect)

sealed class BoxScale(
  val getCrop: (from: BoundRect, to: BoundRect) -> Crop?,
) {
  fun pasteScaled(from: Mat, to: Mat) {
    val (srcFrame, destFrame) = getCrop(from.bounds, to.bounds) ?: return
    destFrame.boundsIntersection(to.bounds) ?: return
    val croppedSrc = from.crop(srcFrame)

    val scaledSrc = croppedSrc.resize(destFrame.size)

    scaledSrc.copyTo(to, destFrame.topLeft)
  }

  fun scale(from: Mat, to: BoundRect): Mat = to.toEmptyMat(from.getFormat()).apply {
    pasteScaled(from, this)
  }
}


class Anchored(val anchor: Anchor, val scale: Point = Point(1, 1)) : BoxScale(
  getCrop = { from, to ->
    Crop(
      from,
      anchor.moveAndScale(from, to, scale),
    )
  },
)

class Centered() : BoxScale(
  getCrop = { from, to ->
    Crop(from, Anchor.Center.moveAndScale(from, to, Point(1, 1)))
  },
)

class CenterFit() : BoxScale(
  getCrop = { from, to ->
    val sizeRatio = from.size / to.size
    val tallerAspectRatioThanContainer = sizeRatio.y >= sizeRatio.x

    if (tallerAspectRatioThanContainer) from.scaleByHeight(to.height.toInt())
    else from.scaleByWidth(to.width.toInt())
    Crop(from, Anchor.Center.moveAndScale(from, to, Point(1, 1)))
  },
)

