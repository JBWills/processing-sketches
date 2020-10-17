package sketches

import BaseSketch
import SketchConfig
import controls.Control
import coordinate.BoundRect
import coordinate.Point
import java.awt.Color

class StarterConfig : SketchConfig()

open class StarterSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864
) : BaseSketch<StarterConfig>(
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

  override fun getControls() = listOf<Control>()

  override fun getRandomizedConfig() = StarterConfig()

  override fun drawOnce(config: StarterConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()


    rect(drawBound)
  }
}

fun main() = BaseSketch.run(StarterSketch())