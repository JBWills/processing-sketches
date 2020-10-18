package sketches

import BaseSketch
import SketchConfig
import appletExtensions.intersection
import controls.Control
import controls.Control.Slider
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import java.awt.Color

class ArcTestConfig : SketchConfig()

open class ArcTestSketch(
  var startAngle: Float = 0f,
  var length: Float = 360f,
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch<ArcTestConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "sketches.StarterSketch",
  sketchConfig = null,
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  var position: Point = center
  var size: Float = 50f

  private val outerPaddingX: Float = sizeX * 0.05f
  private val outerPaddingY: Float = sizeY * 0.05f
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf<Control>(
    Slider("Start angle", Pair(0f, 360f), startAngle) {
      startAngle = it
      markDirty()
    },
    Slider("length", Pair(0f, 360f), length) {
      length = it
      markDirty()
    },
    Slider("posX", Pair(0f, sizeX.toFloat()), position.x) {
      position.x = it
      markDirty()
    },
    Slider("posY", Pair(0f, sizeY.toFloat()), position.y) {
      position.y = it
      markDirty()
    },
    Slider("size", Pair(0f, 150f), size) {
      size = it
      markDirty()
    },
  )

  override fun getRandomizedConfig() = ArcTestConfig()

  override fun drawOnce(config: ArcTestConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(2f)
    noFill()

    val baseCircle = Circ(center, 90f)
    val moveCircle = Circ(position, size)
    val clippedCircle = moveCircle.intersection(baseCircle)

    circle(baseCircle)



    arc(clippedCircle)

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(ArcTestSketch())