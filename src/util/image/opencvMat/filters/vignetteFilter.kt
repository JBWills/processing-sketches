package util.image.opencvMat.filters

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc.getGaussianKernel
import util.image.opencvMat.copy
import util.image.opencvMat.divide
import util.image.opencvMat.max

private const val BaseVignettePower = 900

fun Mat.vignetteFilter(power: Double, inPlace: Boolean = false): Mat {
  val colKernel = getGaussianKernel(cols(), BaseVignettePower * power)
  val rowKernel = getGaussianKernel(rows(), BaseVignettePower * power)
  val destMat = if (inPlace) this else copy()

  val product = Mat().also {
    Core.gemm(rowKernel, colKernel.t(), 1.0, Mat(), 0.0, it)
    it.divide(it.max, inPlace = true)
    it.convertTo(it, type(), 255.0)
  }

  return destMat.mul(product)
}
