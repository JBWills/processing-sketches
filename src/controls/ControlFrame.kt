package controls

import appletExtensions.setTabs
import controlP5.ControlP5
import controlP5.Tab
import coordinate.PaddingRect
import coordinate.PixelPoint
import coordinate.Point
import processing.core.PApplet
import java.awt.Color

class ControlFrame(
  private val w: Int,
  private val h: Int,
  private var tabs: List<ControlTab>,
) : PApplet() {

  private val cp5: ControlP5 by lazy {
    ControlP5(this).apply {
      setTabs(tabs, activeTab = tabs.firstOrNull())
    }
  }

  private val padding = PaddingRect(
    vertical = 20,
  )

  private val elementPadding = PaddingRect(
    vertical = 15,
    left = 15,
    right = 15,
  )

  fun updateControls(newControls: List<ControlTab>, selectedIndex: Int = 0) {
    setTabs(newControls)

    newControls.forEach(this::setupTab)

    newControls.getOrNull(selectedIndex)?.let { setActiveTab(it.name) }
  }

  private fun setTabs(newTabs: List<ControlTab>) {
    tabs = newTabs
    cp5.setTabs(tabs, activeTab = tabs.firstOrNull())
  }

  fun setActiveTab(tabName: String) = cp5.controlWindow.tabs.get()
    .map { it as Tab }
    .forEach { tab -> tab.isActive = tab.name == tabName }

  override fun settings() {
    size(w, h)
  }

  private fun setupTab(tab: ControlTab) {
    val controlGroups = tab.controlSections

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
