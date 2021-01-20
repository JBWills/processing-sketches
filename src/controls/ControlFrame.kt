package controls

import controlP5.ControlP5
import coordinate.PaddingRect
import coordinate.PixelPoint
import coordinate.Point
import processing.core.PApplet
import java.awt.Color

class ControlTab(val name: String, val controlGroups: List<ControlGroup>) {
  constructor(name: String, vararg groupables: ControlGroupable) : this(name, groupables.toList().toControlGroups())
}

class ControlFrame(
  private val w: Int,
  private val h: Int,
  private var tabs: List<ControlTab>,
) : PApplet() {

  private fun setTabs(newTabs: List<ControlTab>) {
    tabs = newTabs
    tabs.forEach {
      //cp5.getTab(it.name).close()
      cp5.addTab(it.name)
    }
  }

  private val cp5: ControlP5 by lazy {
    val c = ControlP5(this)

    tabs.forEachIndexed { index, controlTab ->
      if (index == 0) {
        c.getTab("default").setLabel(controlTab.name)
      } else {
        c.addTab(controlTab.name)
      }
    }

    c.window.activateTab(tabs.last().name)

    c
  }

  private val padding = PaddingRect(
    vertical = 20,
  )

  private val elementPadding = PaddingRect(
    vertical = 15,
    left = 15,
    right = 15,
  )

  fun updateControls(newControls: List<ControlTab>) {
    setTabs(newControls)

    newControls.forEach { setupTab(it) }
  }

  override fun settings() {
    size(w, h)
  }

  private fun setupTab(tab: ControlTab) {
    val controlGroups = tab.controlGroups

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
      controlGroup.controls.forEachIndexed { index, control ->
        currentX += elementPadding.left
        control.applyToControl(
          cp5,
          cp5.getTab(tab.name),
          Point(currentX, currentY),
          PixelPoint(elementWidth.toInt(), elementHeight.toInt())
        )
        currentX += elementWidth + elementPadding.right
      }

      currentY += elementHeight + elementPadding.bottom
    }
  }

  override fun setup() {
    tabs.forEach { setupTab(it) }
  }

  override fun draw() {
    background(Color.BLACK.rgb)
  }

  init {
    runSketch(arrayOf(this.javaClass.name), this)
  }
}