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
  private val controlGroups: List<ControlGroup>,
) : PApplet() {

  private val cp5: ControlP5 by lazy { ControlP5(this) }

  private val padding = PaddingRect(
    vertical = 20,
  )

  private val elementPadding = PaddingRect(
    vertical = 15,
    left = 15,
    right = 15,
  )

  override fun settings() {
    size(w, h)
  }

  override fun setup() {
    surface.setLocation(10, 10)
    val usableHeight = h - padding.totalVertical()

    var currentY = padding.top

    val totalElementPaddingHeight = controlGroups.size * elementPadding.totalVertical()

    // height for an element with a ratio of 1.
    val elementBaseHeight = (usableHeight - totalElementPaddingHeight) / controlGroups.totalRatio()

    controlGroups.forEach { controlGroup ->
      currentY += elementPadding.top
      val rowWidth = w - padding.totalHorizontal()
      val elementWidth = (rowWidth / controlGroup.size) - elementPadding.totalHorizontal()

      val elementHeight = elementBaseHeight * controlGroup.heightRatio.toDouble()

      var currentX = padding.left
      controlGroup.controls.forEach { control ->
        currentX += elementPadding.left
        control.applyToControl(
          cp5,
          Point(currentX, currentY),
          PixelPoint(elementWidth.toInt(), elementHeight.toInt())
        )
        currentX += elementWidth + elementPadding.right
      }

      currentY += elementHeight + elementPadding.bottom
    }
  }

  override fun draw() {
    background(Color.BLACK.rgb)
  }

  init {
    runSketch(arrayOf(this.javaClass.name), this)
  }
}