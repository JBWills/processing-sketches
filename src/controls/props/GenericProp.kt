package controls.props

import controls.panels.ControlPanel
import controls.panels.Panelable
import kotlin.reflect.KMutableProperty0

open class GenericProp<T>(
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter: () -> Panelable
) : ControlPanelProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)

  override fun toControlPanel(): ControlPanel = controlsGetter().toControlPanel()

  companion object {
    fun <T> prop(
      ref: KMutableProperty0<T>,
      controlsGetter: () -> Panelable,
    ) = GenericProp(ref, controlsGetter = controlsGetter)

    fun <T : PropData<T>> prop(
      ref: KMutableProperty0<T>
    ) = prop(ref) { ref.get().asControlPanel() }
  }
}
