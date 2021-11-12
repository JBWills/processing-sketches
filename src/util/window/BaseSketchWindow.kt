package util.window

import BaseSketch
import controls.ControlFrame
import controls.panels.ControlTab
import coordinate.Point
import processing.core.PSurface
import java.awt.Cursor
import kotlin.math.max

class BaseSketchWindow(private val title: String, private val surface: PSurface) {
  private var loadingCount: Int = 0
    set(value) {
      val wasLoading = field != 0
      val isLoading = value != 0
      field = value

      if (wasLoading == isLoading) return

      if (isLoading) {
        surface.setTitle("$title + (Loading)")
        surface.setCursor(Cursor.WAIT_CURSOR)
      } else {
        surface.setTitle(title)
        surface.setCursor(Cursor.DEFAULT_CURSOR)
      }
    }

  var controlFrame: ControlFrame? = null

  val isLoading get() = loadingCount > 0

  fun setLoadingStarted() {
    loadingCount++
  }

  fun setLoadingEnded() {
    loadingCount = max(loadingCount - 1, 0)
  }

  fun updateControls(
    sketch: BaseSketch,
    size: Point,
    newControls: List<ControlTab>
  ) {
    val lastControlFrame = controlFrame
    val (tab, index) = lastControlFrame?.getActiveTabAndIndex() ?: (null to 0)
    lastControlFrame?.close()

    controlFrame = ControlFrame(sketch, size.xi, size.yi, newControls).also {
      var newTabIndex = it.indexOfTab(tab?.name) ?: index
      if (newTabIndex >= it.numTabs()) newTabIndex = 0

      it.setActiveTab(newTabIndex)
    }
  }
}
