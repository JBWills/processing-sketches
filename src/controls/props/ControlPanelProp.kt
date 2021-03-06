package controls.props

import controls.panels.ControlPanel
import controls.panels.Panelable

interface ControlPanelProp<T> : Panelable {
  val name: String

  fun get(): T
  fun set(newVal: T)
  override fun toControlPanel(): ControlPanel
}
