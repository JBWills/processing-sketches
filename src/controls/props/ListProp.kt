package controls.props

import controls.panels.ControlPanel
import controls.panels.Panelable

open class ListProp<T>(
  private var list: MutableList<T>,
  private var listIndex: Int,
  override val name: String,
  private val controlsGetter: () -> Panelable,
) : ControlPanelProp<T> {
  override fun get(): T = list[listIndex]
  override fun set(newVal: T) {
    list[listIndex] = newVal
  }

  override fun toControlPanel(): ControlPanel = controlsGetter().toControlPanel()

  companion object {
    fun <T> listProp(
      ref: MutableList<T>,
      listIndex: Int,
      controlsGetter2: () -> Panelable,
    ) = ListProp(
      ref,
      listIndex,
      name = "List Reference: $listIndex",
      controlsGetter = controlsGetter2,
    )
  }
}
