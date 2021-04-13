package sketches.legacy

import BaseSketch
import LayerConfig
import appletExtensions.intersection
import controls.doublePairProp
import controls.doubleProp
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import java.awt.Color

open class ArcTestSketch(
  var startAngle: Double = 0.0,
  var length: Double = 360.0,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.StarterSketch",
  sizeX = sizeX,
  sizeY = sizeY,
) {

  var position: Point = center
  var size: Double = 50.0

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeX - 2 * outerPaddingX,
    sizeY - 2 * outerPaddingY
  )

  override fun getControls(): Panelable = col(
    doubleProp(::startAngle, 0..360),
    doubleProp(::length, 0..360),
    doublePairProp(::position, 0.0..sizeX.toDouble()),
    doubleProp(::size, 0..150),
  )

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

fun main() = ArcTestSketch().run()
