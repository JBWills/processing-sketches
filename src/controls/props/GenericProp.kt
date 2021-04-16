package controls.props

import BaseSketch
import controls.panels.ControlPanel
import controls.panels.Panelable
import kotlin.reflect.KMutableProperty0

open class GenericProp<T>(
  override val sketch: BaseSketch,
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter2: () -> Panelable
) : ControlPanelProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)

  override fun toControlPanel(): ControlPanel = controlsGetter2().toControlPanel()

  companion object {
    fun <T> BaseSketch.prop(
      ref: KMutableProperty0<T>,
      controlsGetter2: () -> Panelable,
    ) = GenericProp(
      this,
      ref,
      controlsGetter2 = controlsGetter2,
    )

    fun <T : PropData<T>> BaseSketch.prop(
      ref: KMutableProperty0<T>
    ) = prop(ref) { ref.get().asControlPanel(this) }
  }
}
