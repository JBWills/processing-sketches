package controls.props

import BaseSketch
import controls.panels.ControlPanel
import controls.panels.Panelable

open class ListProp<T>(
  override val sketch: BaseSketch,
  private var list: MutableList<T>,
  private var listIndex: Int,
  override val name: String,
  private val controlsGetter2: () -> Panelable,
) : ControlPanelProp<T> {
  override fun get(): T = list[listIndex]
  override fun set(newVal: T) {
    list[listIndex] = newVal
  }

  override fun toControlPanel(): ControlPanel = controlsGetter2().toControlPanel()

  companion object {
    fun <T> BaseSketch.listProp(
      ref: MutableList<T>,
      listIndex: Int,
      controlsGetter2: () -> Panelable,
    ) = ListProp(
      this,
      ref,
      listIndex,
      name = "List Reference: $listIndex",
      controlsGetter2 = controlsGetter2,
    )
  }
}
