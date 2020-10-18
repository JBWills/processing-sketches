package controls

import controlP5.ControlP5
import coordinate.PaddingRect
import coordinate.PixelPoint
import coordinate.Point
import processing.core.PApplet
import java.awt.Color

class ControlFrame(
  private val w: Int,
  private val h: Int,
  private val controls: List<Control>,
) : PApplet() {
  private val cp5: ControlP5 by lazy { ControlP5(this) }

  private val padding = PaddingRect(
    base = 30f,
    left = 10f,
    right = 80f
  )

  private val elementPadding = PaddingRect(
    vertical = 15f,
  )

  override fun settings() {
    size(w, h)
  }

  override fun setup() {
    surface.setLocation(10, 10)
    val usableHeight = h - padding.totalVertical()
    val elementWidth = w - padding.totalHorizontal() - elementPadding.totalHorizontal()
    val elementHeight = (usableHeight / controls.size) - elementPadding.totalVertical()

    var currentY = padding.top

    controls.forEach { control ->
      val positionY = currentY + elementPadding.top
      val position = Point(padding.left + elementPadding.left, positionY)
      val size = PixelPoint(elementWidth.toInt(), elementHeight.toInt())
      control.applyToControl(cp5, position, size)

      currentY = positionY + elementHeight + elementPadding.bottom
    }
  }

  override fun draw() {
    background(Color.BLACK.rgb)
  }

  init {
    runSketch(arrayOf(this.javaClass.name), this)
  }
}