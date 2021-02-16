package sketches

import BaseSketch
import LayerConfig
import appletExtensions.intersection
import controls.Control
import controls.Control.Slider
import controls.ControlGroupable
import controls.toControlGroups
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import java.awt.Color

open class ArcTestSketch(
  var startAngle: Double = 0.0,
  var length: Double = 360.0,
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.StarterSketch",
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  var position: Point = center
  var size: Double = 50.0

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls(): List<ControlGroupable> = listOf<Control>(
    Slider("Start angle", 0.0..360.0, startAngle) {
      startAngle = it
      markDirty()
    },
    Slider("length", 0.0..360.0, length) {
      length = it
      markDirty()
    },
    Slider("posX", 0.0..sizeX.toDouble(), position.x) {
      position.x = it
      markDirty()
    },
    Slider("posY", 0.0..sizeY.toDouble(), position.y) {
      position.y = it
      markDirty()
    },
    Slider("size", 0.0..150.0, size) {
      size = it
      markDirty()
    },
  ).toControlGroups()

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(2f)
    noFill()

    val baseCircle = Circ(center, 90.0)
    val moveCircle = Circ(position, size)
    val clippedCircle = moveCircle.intersection(baseCircle)

    circle(baseCircle)



    arc(clippedCircle)

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(ArcTestSketch())
