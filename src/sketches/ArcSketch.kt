package sketches

import BaseSketch
import LayerConfig
import appletExtensions.intersection
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.pow
import java.awt.Color

open class ArcSketch(
  backgroundColor: Color = Color.WHITE,
  size: Point = Point(11 * 72, 16 * 72)
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.ArcSketch",
  size = size,
) {
  var steps = 312.0
  var step = 4.0
  var ratio = 0.92
  var centerHeight = 180.0
  var occludingSize = 246.0
  var occludingHeight = 251.0
  var power = 1.1
  var power2 = 1.1

  private val outerPaddingX: Double = size.x * 0.02
  private val outerPaddingY: Double = size.y * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    size.x - 2 * outerPaddingX,
    size.y - 2 * outerPaddingY,
  )

  override fun getControls(): Panelable = col {
    slider(::steps, 20.0..10560.0)
    slider(::step, 1.0..20.0)
    slider(::ratio, -2.0..2.0)
    slider(::centerHeight, -400.0..600.0)
    slider(::occludingSize, 10.0..600.0)
    slider(::occludingHeight, -400.0..600.0)
    slider(::power, 0.0..2.0)
    slider(::power2, 0.0..2.0)
  }

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()

    val occludingCircle = Circ(center.addY(occludingHeight), occludingSize)

    val centerYBase = center.y - centerHeight
    (step.toInt()..steps.toInt() step step.toInt()).forEach {
      val radius = (it / 2.0).pow(power)
      val origin = Point(center.x, centerYBase + (it / 2.0).pow(power2) * ratio)

      val arcToDraw = Circ(origin, radius).intersection(occludingCircle)

      boundArc(arcToDraw, drawBound)
    }

    rect(drawBound)
  }
}

fun main() = ArcSketch().run()
