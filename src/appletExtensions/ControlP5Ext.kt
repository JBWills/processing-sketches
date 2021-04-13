package appletExtensions

import controlP5.ControlP5
import controlP5.ControlWindow
import controlP5.ControllerInterface
import controlP5.ControllerList
import controlP5.Tab
import controls.panels.ControlTab
import util.with

fun <R> ControllerList.map(block: (ControllerInterface<*>) -> R): List<R> =
  (0 until size()).map { block(get(it)) }

val ControlWindow.controlTabs get() = tabs.get().map { it as Tab }

fun ControlP5.addTabs(tabs: List<ControlTab>) = tabs.forEach {
  addTab(it.name)
}

fun ControlP5.setTabs(
  tabs: List<ControlTab>,
  activeTab: ControlTab? = tabs.firstOrNull(),
) {
  // Can't delete the first tab because of annoying controlP5
  // currentPointer bug
  controlWindow.controlTabs.with {
    slice(1 until size).forEach { it.remove() }
  }

  controlWindow.controlTabs[0].setLabel(tabs.last().name)

  addTabs(tabs.slice(0 until tabs.size - 1))
  activeTab?.name?.let { controlWindow.activateTab(it) }
}
