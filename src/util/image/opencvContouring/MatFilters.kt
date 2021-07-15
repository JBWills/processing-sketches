package util.image.opencvContouring

import coordinate.BoundRect
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import util.image.ChannelDepth
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.ImageFormat.Gray
import util.image.OpenCVThresholdType
import util.image.OpenCVThresholdType.ThreshBinary
import util.image.cloneEmpty

fun Mat.applyWithDest(
  format: ImageFormat = getFormat(),
  block: (src: Mat, dest: Mat) -> Unit,
): Mat = cloneEmpty(format).also { dest -> block(this, dest) }

fun Mat.canny(threshold: Double): Mat = applyWithDest(Gray) { src, dest ->
  Imgproc.Canny(src, dest, threshold, threshold * CannyRatio, 3, true)
}

fun Mat.threshold(value: Double, type: OpenCVThresholdType = ThreshBinary): Mat =
  applyWithDest(Gray) { src, dest ->
    Imgproc.threshold(src, dest, value, 1.0, type.typeVal)
    dest.convertTo(dest, ChannelDepth.CV_8U.typeVal, 255.0)
  }

fun Mat.submat(bounds: BoundRect): Mat =
  submat(Rect(bounds.leftPx, bounds.topPx, bounds.widthPx + 1, bounds.heightPx + 1))
