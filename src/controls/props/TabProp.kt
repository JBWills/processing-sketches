package controls.props

import controls.panels.ControlTab
import kotlin.reflect.KMutableProperty0

interface TabProp<T> {
  val name: String

  fun get(): T
  fun set(newVal: T)
  fun toTabs(): List<ControlTab>
}

open class GenericTabProp<T>(
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter: (backingField: T) -> List<ControlTab>
) : TabProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)
  override fun toTabs(): List<ControlTab> = controlsGetter(get())
}

open class ListTabProp<T>(
  private var list: MutableList<T>,
  private var listIndex: Int,
  override val name: String,
  private val controlsGetter: (backingField: T) -> List<ControlTab>
) : TabProp<T> {
  override fun get(): T = list[listIndex]
  override fun set(newVal: T) {
    list[listIndex] = newVal
  }

  override fun toTabs() = controlsGetter(get())
}

fun <T> tabProp(
  ref: KMutableProperty0<T>,
  controlsGetter: (backingField: T) -> List<ControlTab>
) = GenericTabProp(ref, controlsGetter = controlsGetter)

fun <T> tabProp(
  ref: MutableList<T>,
  listIndex: Int,
  controlsGetter: (backingField: T) -> List<ControlTab>
) = ListTabProp(
  ref,
  listIndex,
  name = "List Reference: $listIndex",
  controlsGetter = controlsGetter,
)
