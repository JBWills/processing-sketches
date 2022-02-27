@file:Suppress("unused")

package util.image.opencvMat

import org.bytedeco.opencv.global.opencv_core
import org.opencv.core.Core.FILLED
import org.opencv.core.Core.LINE_4
import org.opencv.core.Core.LINE_8
import org.opencv.core.Core.LINE_AA
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.HISTCMP_BHATTACHARYYA
import org.opencv.imgproc.Imgproc.HISTCMP_CHISQR
import org.opencv.imgproc.Imgproc.HISTCMP_CHISQR_ALT
import org.opencv.imgproc.Imgproc.HISTCMP_CORREL
import org.opencv.imgproc.Imgproc.HISTCMP_HELLINGER
import org.opencv.imgproc.Imgproc.HISTCMP_INTERSECT
import org.opencv.imgproc.Imgproc.HISTCMP_KL_DIV

enum class OpenCVThresholdType(val type: Int) {
  ThreshBinary(Imgproc.THRESH_BINARY),
  ThreshBinaryInv(Imgproc.THRESH_BINARY_INV),
  ThreshTrunc(Imgproc.THRESH_TRUNC),
  ThreshToZero(Imgproc.THRESH_TOZERO),
  ThreshToZeroInv(Imgproc.THRESH_TOZERO_INV),
  ThreshMask(Imgproc.THRESH_MASK),
  ThreshOTSU(Imgproc.THRESH_OTSU),
  ThreshTriangle(Imgproc.THRESH_TRIANGLE),
}

enum class ContourRetrievalMode(val type: Int) {
  External(Imgproc.RETR_EXTERNAL),
  ListMode(Imgproc.RETR_LIST),
  CComp(Imgproc.RETR_CCOMP),
  Tree(Imgproc.RETR_TREE),
  FloodFill(Imgproc.RETR_FLOODFILL),
}

enum class ContourApproximationMode(val type: Int) {
  None(Imgproc.CHAIN_APPROX_NONE),
  Simple(Imgproc.CHAIN_APPROX_SIMPLE),
  Tc89L1(Imgproc.CHAIN_APPROX_TC89_L1),
  TC89KCOS(Imgproc.CHAIN_APPROX_TC89_KCOS),
}

enum class ChannelDepth(val type: Int, val byteDepth: Int) {
  CV_8U(opencv_core.CV_8U, 1),
  CV_8S(opencv_core.CV_8S, 1),
  CV_16U(opencv_core.CV_16U, 2),
  CV_16S(opencv_core.CV_16S, 2),
  CV_32S(opencv_core.CV_32S, 4),
  CV_32F(opencv_core.CV_32F, 4),
  CV_64F(opencv_core.CV_64F, 8),
  CV_16F(opencv_core.CV_16F, 2),
  ;

  companion object {
    fun Mat.channelDepth(): ChannelDepth {
      val depth = depth()
      return values().first { depth == type() }
    }
  }
}

enum class BorderType(val type: Int) {
  /** {@code iiiiii|abcdefgh|iiiiiii}  with some specified {@code i} */
  BorderConstant(opencv_core.BORDER_CONSTANT),

  /** {@code aaaaaa|abcdefgh|hhhhhhh} */
  BorderReplicate(opencv_core.BORDER_REPLICATE),

  /** {@code fedcba|abcdefgh|hgfedcb} */
  BorderReflect(opencv_core.BORDER_REFLECT),

  /** {@code cdefgh|abcdefgh|abcdefg} */
  BorderWrap(opencv_core.BORDER_WRAP),

  /** {@code gfedcb|abcdefgh|gfedcba} */
  BorderReflect101(opencv_core.BORDER_REFLECT_101),

  /** {@code uvwxyz|abcdefgh|ijklmno} */
  BorderTransparent(opencv_core.BORDER_TRANSPARENT),

  /** same as BORDER_REFLECT_101 */
  BorderDefault(opencv_core.BORDER_DEFAULT),

  /** do not look outside of ROI */
  BorderIsolated(opencv_core.BORDER_ISOLATED),
}

enum class LineFillType(val type: Int) {
  Filled(FILLED),

  /** 4-connected line */
  Line4(LINE_4),

  /** 8-connected line */
  Line8(LINE_8),

  /** antialiased line */
  LineAA(LINE_AA),
}

enum class HistCompareMethod(val type: Int) {
  Correl(HISTCMP_CORREL),
  Chisor(HISTCMP_CHISQR),
  Intersect(HISTCMP_INTERSECT),
  Bhattacharyya(HISTCMP_BHATTACHARYYA),
  Hellinger(HISTCMP_HELLINGER),
  ChisorAlt(HISTCMP_CHISQR_ALT),
  KlDiv(HISTCMP_KL_DIV),
  ;

  companion object {
    val fastestMethod get() = Intersect
  }
}

enum class MorphShapes(val type: Int) {
  Rect(Imgproc.MORPH_RECT),
  Cross(Imgproc.MORPH_CROSS),
  Ellipse(Imgproc.MORPH_ELLIPSE),
}

enum class MorphTypes(val type: Int) {
  Erode(Imgproc.MORPH_ERODE),
  Dilate(Imgproc.MORPH_DILATE),
  Open(Imgproc.MORPH_OPEN),
  Close(Imgproc.MORPH_CLOSE),
  Gradient(Imgproc.MORPH_GRADIENT),
  TopHat(Imgproc.MORPH_TOPHAT),
  BlackHat(Imgproc.MORPH_BLACKHAT),
  HitMiss(Imgproc.MORPH_HITMISS),
}
