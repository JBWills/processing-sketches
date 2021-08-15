package util.image.opencvMat

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun Mat.compareHist(
  other: Mat,
  method: HistCompareMethod = HistCompareMethod.fastestMethod
) = Imgproc.compareHist(this, other, method.type)
