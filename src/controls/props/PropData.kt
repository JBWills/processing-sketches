package controls.props

import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import controls.panels.ControlTab
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable

interface PropData<T> : Bindable, Copyable<T>, KSerializable<T> {
  fun asControlPanel(): ControlPanel = col {
    +bind().map { it.panel }
  }.toControlPanel()

  fun asControlTabs(): List<ControlTab> = bind()
}
