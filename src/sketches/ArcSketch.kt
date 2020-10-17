package sketches

import BaseSketch
import SketchConfig
import controls.Control
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import java.awt.Color

class ArcConfig : SketchConfig()

open class ArcSketch(
  var startAngle: Float = 0f,
  var length: Float = 360f,
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch<ArcConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "sketches.StarterSketch",
  sketchConfig = null,
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  private val outerPaddingX: Float = sizeX * 0.05f
  private val outerPaddingY: Float = sizeY * 0.05f
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf<Control>(
    Control.Slider("Start angle", Pair(0f, 360f)) {
      startAngle = it
      markDirty()
    },
    Control.Slider("length", Pair(0f, 360f)) {
      length = it
      markDirty()
    },
  )

  override fun getRandomizedConfig() = ArcConfig()

  override fun drawOnce(config: ArcConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(2f)
    noFill()

    circle(Circ(center, 90f))

    arc(Arc(Deg(startAngle), length, Circ(center, 50f)))

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(ArcSketch())