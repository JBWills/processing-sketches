package controls.props

import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable

interface PropData<T> : Bindable, Copyable<T>, KSerializable<T> {
  fun asControlPanel(): ControlPanel = col {
    +bind().map { it.panel }
  }.toControlPanel()
}
