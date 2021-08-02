@file:Suppress("unused")

package util.image.opencvMat

import org.bytedeco.opencv.global.opencv_core
import org.opencv.imgproc.Imgproc

enum class OpenCVThresholdType(val typeVal: Int) {
  ThreshBinary(Imgproc.THRESH_BINARY),
  ThreshBinaryInv(Imgproc.THRESH_BINARY_INV),
  ThreshTrunc(Imgproc.THRESH_TRUNC),
  ThreshToZero(Imgproc.THRESH_TOZERO),
  ThreshToZeroInv(Imgproc.THRESH_TOZERO_INV),
  ThreshMask(Imgproc.THRESH_MASK),
  ThreshOTSU(Imgproc.THRESH_OTSU),
  ThreshTriangle(Imgproc.THRESH_TRIANGLE),
}

enum class ContourRetrievalModes(val typeVal: Int) {
  External(Imgproc.RETR_EXTERNAL),
  ListMode(Imgproc.RETR_LIST),
  CComp(Imgproc.RETR_CCOMP),
  Tree(Imgproc.RETR_TREE),
  FloodFill(Imgproc.RETR_FLOODFILL),
}

enum class ContourApproximationModes(val typeVal: Int) {
  None(Imgproc.CHAIN_APPROX_NONE),
  Simple(Imgproc.CHAIN_APPROX_SIMPLE),
  Tc89L1(Imgproc.CHAIN_APPROX_TC89_L1),
  TC89KCOS(Imgproc.CHAIN_APPROX_TC89_KCOS),
}

enum class ChannelDepth(val typeVal: Int) {
  CV_8U(opencv_core.CV_8U),
  CV_8S(opencv_core.CV_8S),
  CV_16U(opencv_core.CV_16U),
  CV_16S(opencv_core.CV_16S),
  CV_32S(opencv_core.CV_32S),
  CV_32F(opencv_core.CV_32F),
  CV_64F(opencv_core.CV_64F),
  CV_16F(opencv_core.CV_16F),
}
