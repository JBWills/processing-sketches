package sketches

import BaseSketch
import LayerConfig
import SketchConfig
import controls.Control
import controls.ControlGroupable
import controls.toControlGroups
import coordinate.BoundRect
import coordinate.Point
import java.awt.Color

class StarterConfig : SketchConfig()

open class StarterSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch<StarterConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.StarterSketch",
  sketchConfig = null,
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls(): List<ControlGroupable> = listOf<Control>().toControlGroups()

  override fun getRandomizedConfig() = StarterConfig()

  override fun drawOnce(config: StarterConfig, layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()


    rect(drawBound)
  }
}

fun main() = BaseSketch.run(StarterSketch())