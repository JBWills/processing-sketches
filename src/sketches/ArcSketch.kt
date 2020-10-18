package sketches

import BaseSketch
import SketchConfig
import appletExtensions.intersection
import controls.Control
import controls.Control.Slider
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.times
import java.awt.Color

class ArcConfig : SketchConfig()

open class ArcSketch(
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

  var steps = 150f
  var step = 4f
  var ratio = 0.9f
  var centerHeight = 200f
  var occludingSize = 200f
  var occludingHeight = 200f

  private val outerPaddingX: Float = sizeX * 0.05f
  private val outerPaddingY: Float = sizeY * 0.05f
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf<Control>(
    Slider("Steps", Pair(20f, 360f), steps) {
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
  )

  override fun getRandomizedConfig() = ArcConfig()

  override fun drawOnce(config: ArcConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(2f)
    noFill()

    val occludingCircle = Circ(center.addY(occludingHeight), occludingSize)

    val centerYBase = center.y - centerHeight
    (1..steps.toInt() step step.toInt()).forEach {
      val radius = it
      val origin = Point(center.x, centerYBase + it * ratio)

      arc(Circ(origin, radius).intersection(occludingCircle))
    }

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(ArcSketch())