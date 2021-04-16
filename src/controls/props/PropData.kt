package controls.props

import BaseSketch
import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable
import util.iterators.mapArray

interface PropData<T> : Bindable, Copyable<T>, KSerializable<T> {
  fun asControlPanel(sketch: BaseSketch): ControlPanel = col {
    addAll(bindSketch(sketch).mapArray { it.panel })
  }.toControlPanel()
}
