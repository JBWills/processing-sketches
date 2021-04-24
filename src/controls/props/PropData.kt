package controls.props

import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import geomerativefork.src.util.mapArray
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable

interface PropData<T> : Bindable, Copyable<T>, KSerializable<T> {
  fun asControlPanel(): ControlPanel = col {
    +bind().mapArray { it.panel }.toList()
  }.toControlPanel()
}
