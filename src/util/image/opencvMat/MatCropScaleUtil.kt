package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import coordinate.ShapeTransform
import interfaces.shape.revertTransform
import interfaces.shape.transform
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.Companion.getFormat

fun Mat.crop(crop: BoundRect) = submat(crop)

fun Mat.scale(amount: Number) = resize(size * amount)
fun Mat.scale(amount: Point) = resize(size * amount)

fun Mat.resize(newSize: Point) =
  if (newSize == size) this
  else Mat(newSize.yi, newSize.xi, getFormat().openCVFormat).apply {
    if (empty()) return@apply
    Imgproc.resize(this@resize, this, this.size())
  }

fun Mat.resize(getSize: (size: Point) -> Point) = resize(getSize(size))

fun Mat.getTransformedMat(matToScreenTransform: ShapeTransform, boundRect: BoundRect) =
  bounds
    .transform(matToScreenTransform)
    .boundsIntersection(boundRect)
    ?.revertTransform(matToScreenTransform)
    ?.let { subMatBounds ->

      val scale = matToScreenTransform.transform(BoundRect(Point.Zero, Point.One)).size
      submat(subMatBounds).scale(scale)
    }

fun Mat.rotate(angle: Deg, inPlace: Boolean = false) =
  when {
    angle.value != 0.0 ->
      applyWithDest(inPlace = inPlace) { src, dest ->
        val center = (size / 2).toOpenCvPoint()
        val rotationMatrix = Imgproc.getRotationMatrix2D(center, angle.value, 1.0)
        Imgproc.warpAffine(src, dest, rotationMatrix, size())
      }
    inPlace -> this
    else -> copy()
  }
