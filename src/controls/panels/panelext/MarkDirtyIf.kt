package controls.panels.panelext

import BaseSketch

fun BaseSketch.markDirtyIf(predicate: Boolean) {
  if (predicate) markDirty()
}
