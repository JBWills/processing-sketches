package sketches

import BaseSketch
import SketchConfig
import appletExtensions.intersection
import controls.Control
import controls.Control.Slider
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.pow
import util.squared
import java.awt.Color

class ArcConfig : SketchConfig()

open class ArcSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 11 * 72,
  sizeY: Int = 16 * 72,
) : BaseSketch<ArcConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "sketches.ArcSketch",
  sketchConfig = null,
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  var steps = 312f
  var step = 4f
  var ratio = 0.92f
  var centerHeight = 180f
  var occludingSize = 246f
  var occludingHeight = 251f
  var power = 1.1f
  var power2 = 1.1f

  private val outerPaddingX: Float = sizeX * 0.02f
  private val outerPaddingY: Float = sizeY * 0.02f
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf<Control>(
    Slider("Steps", Pair(20f, 10560f), steps) {
      steps = it
      markDirty()
    },
    Slider("Step", Pair(1f, 20f), step) {
      step = it
      markDirty()
    },
    Slider("ratio", Pair(-2f, 2f), ratio) {
      ratio = it
      markDirty()
    },
    Slider("centerHeight", Pair(-400f, 600f), centerHeight) {
      centerHeight = it
      markDirty()
    },
    Slider("occludingSize", Pair(10f, 600f), occludingSize) {
      occludingSize = it
      markDirty()
    },
    Slider("occludingHeight", Pair(-400f, 600f), occludingHeight) {
      occludingHeight = it
      markDirty()
    },
    Slider("power", Pair(0f, 2f), power) {
      power = it
      markDirty()
    },
    Slider("power2", Pair(0f, 2f), power2) {
      power2 = it
      markDirty()
    },
  )

  override fun getRandomizedConfig() = ArcConfig()

  override fun drawOnce(config: ArcConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()

    val occludingCircle = Circ(center.addY(occludingHeight), occludingSize)

    val centerYBase = center.y - centerHeight
    (step.toInt()..steps.toInt() step step.toInt()).forEach {
      val radius = (it / 2f).pow(power)
      val origin = Point(center.x, centerYBase + (it / 2f).pow(power2) * ratio)

      val arcToDraw = Circ(origin, radius).intersection(occludingCircle)

      boundArc(arcToDraw, drawBound)
    }

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(ArcSketch())