package controls

import BaseSketch
import controls.panels.ControlTab
import kotlin.reflect.KMutableProperty0

interface TabProp<T> {
  val sketch: BaseSketch
  val name: String

  fun get(): T
  fun set(newVal: T)
  fun toTabs(): List<ControlTab>
}

open class GenericTabProp<T>(
  override val sketch: BaseSketch,
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter: BaseSketch.(backingField: T) -> List<ControlTab>
) : TabProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)
  override fun toTabs(): List<ControlTab> = sketch.controlsGetter(get())
}

open class ListTabProp<T>(
  override val sketch: BaseSketch,
  private var list: MutableList<T>,
  private var listIndex: Int,
  override val name: String,
  private val controlsGetter: BaseSketch.(backingField: T) -> List<ControlTab>
) : TabProp<T> {
  override fun get(): T = list[listIndex]
  override fun set(newVal: T) {
    list[listIndex] = newVal
  }

  override fun toTabs() = sketch.controlsGetter(get())
}

fun <T> BaseSketch.tabProp(
  ref: KMutableProperty0<T>,
  controlsGetter: BaseSketch.(backingField: T) -> List<ControlTab>
) = GenericTabProp(this, ref, controlsGetter = controlsGetter)

fun <T> BaseSketch.tabProp(
  ref: MutableList<T>,
  listIndex: Int,
  controlsGetter: BaseSketch.(backingField: T) -> List<ControlTab>
) = ListTabProp(
  this,
  ref,
  listIndex,
  name = "List Reference: $listIndex",
  controlsGetter = controlsGetter,
)
