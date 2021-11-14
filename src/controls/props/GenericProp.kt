package controls.props

import controls.panels.ControlPanel
import controls.panels.Panelable
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.wrapSelf
import kotlin.reflect.KMutableProperty0

open class GenericProp<T>(
  private var ref: RefGetter<T>,
  override val name: String = ref.name,
  private val controlsGetter: () -> Panelable
) : ControlPanelProp<T> {
  constructor(
    ref: KMutableProperty0<T>,
    name: String = ref.name,
    controlsGetter: () -> Panelable
  ) : this(ref.wrapSelf(), name, controlsGetter)

  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)

  override fun toControlPanel(): ControlPanel = controlsGetter().toControlPanel()
}
