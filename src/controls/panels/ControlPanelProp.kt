package controls.panels

import BaseSketch

interface ControlPanelProp<T> : Panelable {
  val sketch: BaseSketch
  val name: String

  fun get(): T
  fun set(newVal: T)
  override fun toControlPanel(): ControlPanel
}
