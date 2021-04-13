package sketches.legacy

import BaseSketch
import LayerConfig
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Point
import java.awt.Color

open class StarterSketch(
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.StarterSketch",
  sizeX = sizeX,
  sizeY = sizeY,
) {

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeX - 2 * outerPaddingX,
    sizeY - 2 * outerPaddingY
  )

  override fun getControls(): Panelable = col()

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()


    rect(drawBound)
  }
}

fun main() = StarterSketch().run()
