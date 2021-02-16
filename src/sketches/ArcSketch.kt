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
import util.pow
import java.awt.Color

open class ArcSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 11 * 72,
  sizeY: Int = 16 * 72,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.ArcSketch",
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  var steps = 312.0
  var step = 4.0
  var ratio = 0.92
  var centerHeight = 180.0
  var occludingSize = 246.0
  var occludingHeight = 251.0
  var power = 1.1
  var power2 = 1.1

  private val outerPaddingX: Double = sizeX * 0.02
  private val outerPaddingY: Double = sizeY * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls(): List<ControlGroupable> = listOf<Control>(
    Slider("Steps", 20.0..10560.0, steps) {
      steps = it
      markDirty()
    },
    Slider("Step", 1.0..20.0, step) {
      step = it
      markDirty()
    },
    Slider("ratio", -2.0..2.0, ratio) {
      ratio = it
      markDirty()
    },
    Slider("centerHeight", -400.0..600.0, centerHeight) {
      centerHeight = it
      markDirty()
    },
    Slider("occludingSize", 10.0..600.0, occludingSize) {
      occludingSize = it
      markDirty()
    },
    Slider("occludingHeight", -400.0..600.0, occludingHeight) {
      occludingHeight = it
      markDirty()
    },
    Slider("power", 0.0..2.0, power) {
      power = it
      markDirty()
    },
    Slider("power2", 0.0..2.0, power2) {
      power2 = it
      markDirty()
    },
  ).toControlGroups()

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
