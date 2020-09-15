import controls.Control.Button
import controls.Control.Slider
import controls.Control.Toggle
import coordinate.Point
import processing.core.PApplet
import processing.core.PConstants
import util.times
import util.toRGBInt
import java.awt.Color

class BasePApplet : PApplet() {
  companion object Factory {
    const val SIZE_X: Int = 1000
    const val SIZE_Y: Int = 1000

    fun run() {
      val art = BasePApplet()
      art.setSize(SIZE_X, SIZE_Y)
      art.runSketch()
    }
  }

  private fun drawCircle(center: Point, size: Number, strokeColor: Color = Color.black) {
    stroke(strokeColor.rgb)
    strokeWeight(0.5f)
    noFill()
    circle(center.x, center.y, size.toFloat())
  }

  private fun drawConcentricCircles(center: Point, distanceBetween: Float, strokeColor: Color = Color.black) {
    val numToDraw = (max(SIZE_X, SIZE_Y) * 4 / distanceBetween).toInt()
    numToDraw.times { i -> drawCircle(center, (i.toFloat() * distanceBetween), strokeColor) }
  }

  fun onButtonClick() {
    kotlin.io.println("button clicked")
  }

  fun onToggle(value: Boolean) {
    kotlin.io.println("button toggled: $value")
  }

  fun onSliderChange(value: Float) {
    kotlin.io.println("slider changed: $value")
  }

  override fun setup() {
    ControlFrame(400, 800, listOf(
      Button("Choose file", this::onButtonClick),
      Button("Save frame", this::onButtonClick),
      Toggle("auto", this::onToggle),
      Toggle("blend", this::onToggle),
      Slider("speed", 0f to 100f, this::onSliderChange),
    ))

    colorMode(PConstants.RGB, 255f, 255f, 255f, 255f)
    noStroke()
    background("#FFFFFF".toRGBInt())

    val center = Point(SIZE_X.toFloat() / 2, SIZE_Y.toFloat() / 2)
    20.times {
      drawConcentricCircles(center + Point(SIZE_X * 3f * (it / 20f) - SIZE_X, 0f), 20f, Color.BLACK)
    }
  }
}

fun main(args: Array<String>) = BasePApplet.run()