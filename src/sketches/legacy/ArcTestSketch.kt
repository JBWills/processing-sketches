package sketches.legacy

import BaseSketch
import LayerConfig
import appletExtensions.draw.arc
import appletExtensions.draw.circle
import appletExtensions.draw.rect
import appletExtensions.intersection
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.panelext.sliderPair
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import java.awt.Color

open class ArcTestSketch(
  var startAngle: Double = 0.0,
  var length: Double = 360.0,
  backgroundColor: Color = Color.BLACK,
  size: Point = Point(576, 864),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.StarterSketch",
  size = size,
) {

  var position: Point = center
  var arcSize: Double = 50.0

  private val outerPaddingX: Double = size.x * 0.05
  private val outerPaddingY: Double = size.y * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    size.x - 2 * outerPaddingX,
    size.y - 2 * outerPaddingY,
  )

  override fun getControls(): Panelable = col {
    slider(::startAngle, 0..360)
    slider(::length, 0..360)
    sliderPair(::position, 0.0..size.x)
    slider(::arcSize, 0..150)
  }

  override suspend fun SequenceScope<Unit>.drawOnce(
    layer: Int,
    layerConfig: LayerConfig,
  ) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(2f)
    noFill()

    val baseCircle = Circ(center, 90.0)
    val moveCircle = Circ(position, arcSize)
    val clippedCircle = moveCircle.intersection(baseCircle)

    circle(baseCircle)

    arc(clippedCircle)

    rect(drawBound)
  }
}

fun main() = ArcTestSketch().run()
