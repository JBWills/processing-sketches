package controls

import appletExtensions.addSolidColorRectangle
import appletExtensions.setTabs
import controlP5.ControlP5
import controlP5.Tab
import controls.panels.ControlItem
import controls.panels.ControlList
import controls.panels.ControlPanel
import controls.panels.ControlTab
import coordinate.BoundRect
import coordinate.PaddingRect
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

  private val tabBound: BoundRect get() = BoundRect(Point.Zero, w, h)

  fun getActiveTabAndIndex(): Pair<ControlTab, Int>? {
    val currentTabName = cp5.controlWindow.currentTab.name

    val tabIndex = tabs.indexOfFirst { it.name == currentTabName }
    return if (tabIndex == -1) null else tabs[tabIndex] to tabIndex
  }

  /**
   * Close the window. This is a little hacky because processing doesn't provide a way to
   * exit a child process without exiting the parent as well, so we just shut it down and hide
   * it.
   */
  fun close() {
    noLoop()
    surface.setVisible(false)
  }

  fun indexOfTab(tabName: String): Int? {
    val index = tabs.indexOfFirst { it.name == tabName }
    return if (index == -1) null else index
  }

  fun numTabs() = tabs.size

  fun setActiveTab(tabIndex: Int) = setActiveTab(tabs[tabIndex].name)

  fun setActiveTab(tabName: String) = cp5.controlWindow.tabs.get()
    .map { it as Tab }
    .forEach { tab -> tab.isActive = tab.name == tabName }

  override fun settings() = size(w, h)

  private fun setupTab(tab: ControlTab) = drawPanel(
    tab,
    tab.panel,
    tabBound.minusPadding(FRAME_PADDING),
  )

  private fun drawPanel(tab: ControlTab, panel: ControlPanel, bound: BoundRect) {
    // Add a solid rectangle to the background
    panel.style.frameBackgroundColorOverrides?.let { c ->
      cp5.addSolidColorRectangle("${panel.id} background", tab.name, bound, c)
    }

    when (panel) {
      is ControlTab -> drawPanel(tab, panel.panel, bound)
      is ControlItem -> drawPanelItem(tab, panel, bound)
      is ControlList -> panel.childBounds(bound)
        .forEach { (child, childBound) -> drawPanel(tab, child, childBound) }
    }
  }

  private fun drawPanelItem(controlTab: ControlTab, panelItem: ControlItem, bound: BoundRect) =
    panelItem.draw(cp5, controlTab, bound)

  override fun setup() {
    tabs.forEach { setupTab(it) }
  }

  override fun draw() {
    background(Color.BLACK.rgb)
  }

  init {
    runSketch(arrayOf(this.javaClass.name), this)
  }

  companion object {
    val FRAME_PADDING = PaddingRect(
      base = 15,
      top = 30,
    )
  }
}
