package fastnoise

import coordinate.BoundRect
import org.opencv.core.Mat
import util.image.ImageFormat.Float32
import util.image.opencvMat.createMat
import java.awt.Color

fun Noise.toOpenCVMat(bounds: BoundRect): Mat {
  val xRange = bounds.xPixels
  val width = (xRange.last - xRange.first) + 1
  val yRange = bounds.yPixels
  val height = (yRange.last - yRange.first) + 1

  val mat = createMat(height, width, format = Float32, Color.BLACK)

  val arr = FloatArray(width * height) { i ->
    val x = i % width
    val y = i / width

    (get(x, y) + 0.5).toFloat()
  }

  mat.put(0, 0, arr)

  return mat
}
