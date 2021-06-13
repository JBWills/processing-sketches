package controls.panels.panelext

import BaseSketch

fun BaseSketch.markDirtyIf(pred: Boolean) {
  if (pred) markDirty()
}
