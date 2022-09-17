package util.flowFields

import coordinate.Deg
import coordinate.Point
import coordinate.transforms.ShapeTransform
import fastnoise.Noise
import org.opencv.core.Mat
import util.image.opencvMat.getSubPix

fun Double.toUnitVector(angleScale: Double) = Deg(this * 360 * angleScale).unitVector

sealed interface FlowField {
  fun get(p: Point): Point

  fun get(x: Number, y: Number) = get(Point(x, y))
}

class NoiseFlowField(private val n: Noise, private val angleScale: Double) : FlowField {
  override fun get(p: Point): Point =
    Deg(n.get(p) * 360 * angleScale).unitVector
}

class MatFlowField(
  private val mat: Mat,
  private val screenToMatTransform: ShapeTransform,
  private val angleScale: Double
) :
  FlowField {
  val matToScreenTransform = screenToMatTransform.inverted()
  override fun get(p: Point): Point =
    (mat.getSubPix(screenToMatTransform.transform(p)) ?: 0.0).toUnitVector(angleScale)
}
