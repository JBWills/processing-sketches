package controls

import BaseSketch
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
import util.equalsIgnoreCase
import util.style
import util.window.setLocation
import java.awt.Color

class ControlFrame(
  private val sketch: BaseSketch,
  private val size: Point,
  private var tabs: List<ControlTab>,
  private val startLocation: Point = Point.Zero,
) : PApplet() {
  private val cp5: ControlP5 by lazy {
    ControlP5(this).apply {
      setTabs(tabs, activeTab = tabs.firstOrNull())
    }
  }

  private val tabHeight: Int = 16
  private val tabBound: BoundRect
    get() = BoundRect(size.x, size.y).minusPadding(PaddingRect(top = tabHeight))

  fun getActiveTabAndIndex(): Pair<ControlTab, Int>? {
    val currentTabName = cp5.controlWindow.currentTab?.name ?: return null

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

  fun indexOfTab(tabName: String?): Int? {
    val index = tabs.indexOfFirst { it.name == tabName }
    return if (index == -1) null else index
  }

  fun numTabs() = tabs.size

  fun setActiveTab(tabIndex: Int) = setActiveTab(tabs[tabIndex].name)

  fun setActiveTab(tabName: String) {
    val allTabs = cp5.controlWindow.tabs.get()

    val indexOfTab = allTabs.indexOfFirst { it.name.equalsIgnoreCase(tabName) }
    if (indexOfTab == allTabs.size - 1) {
      throw Exception("Error trying to select tab $tabName at index: $indexOfTab with tabs length: ${allTabs.size}. Sorry, you can't programmatically select the last tab because of a bug in getCurrentTab in controlP5. You should probably just add a dummy tab after this one.")
    }

    allTabs.mapIndexed { index, tab -> (tab as Tab).apply { isActive = index == indexOfTab } }
  }

  override fun settings() = size(size.xi, size.yi)

  private fun setupTab(tab: ControlTab) {
    val style = tab.tabStyle
    cp5.controlWindow.getTab(tab.name)?.style(style)
    style.frameBackgroundColor?.let {
      cp5.addSolidColorRectangle("${tab.name} background", tab.name, tabBound, it)
    }

    drawPanel(
      tab,
      tab.panel,
      tabBound.minusPadding(style.nonNullPadding),
    )
  }

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
    panelItem.draw(sketch, cp5, controlTab, bound)

  override fun setup() {
    setLocation(startLocation)
    tabs.forEach { setupTab(it) }
  }

  override fun draw() {
    background(Color.BLACK.rgb)
  }

  init {
    runSketch(arrayOf(this.javaClass.name), this)
  }
}
