package controls

import BaseSketch
import controls.ControlSection.Companion.toControlSection
import coordinate.Point
import fastnoise.Noise
import util.DoubleRange
import util.propertyEnumDropdown
import util.propertySlider
import util.propertySliderPair
import util.propertyToggle
import util.tuple.and
import kotlin.reflect.KMutableProperty0

/**
 * A control prop is a connection from a field to a single control or group of controls
 */
interface ControlProp<T> : ControlSectionable {
  val sketch: BaseSketch
  val name: String

  fun get(): T
  fun set(newVal: T)
  override fun toControlGroups(): Array<ControlGroupable>
}

open class GenericReferenceField<T>(
  override val sketch: BaseSketch,
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter: BaseSketch.(backingField: T) -> ControlSectionable
) : ControlProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)
  override fun toControlGroups(): Array<ControlGroupable> =
    sketch.controlsGetter(get()).toControlGroups()
}

open class ListReferenceField<T>(
  override val sketch: BaseSketch,
  private var list: MutableList<T>,
  private var listIndex: Int,
  override val name: String,
  private val controlsGetter: BaseSketch.(backingField: T) -> ControlSectionable
) : ControlProp<T> {
  override fun get(): T = list[listIndex]
  override fun set(newVal: T) {
    list[listIndex] = newVal
  }

  override fun toControlGroups(): Array<ControlGroupable> =
    sketch.controlsGetter(get()).toControlGroups()
}

fun <T> BaseSketch.prop(
  ref: KMutableProperty0<T>,
  controlsGetter: BaseSketch.(backingField: T) -> ControlSectionable
) = GenericReferenceField(this, ref, controlsGetter = controlsGetter)

fun <T> BaseSketch.prop(
  ref: MutableList<T>,
  listIndex: Int,
  controlsGetter: BaseSketch.(backingField: T) -> ControlSectionable
) =
  ListReferenceField(
    this,
    ref,
    listIndex,
    name = "List Reference: $listIndex",
    controlsGetter = controlsGetter
  )

fun BaseSketch.booleanProp(ref: KMutableProperty0<Boolean>) =
  prop(ref) { propertyToggle(ref, name = ref.name) }

fun BaseSketch.intProp(ref: KMutableProperty0<Int>, range: IntRange) =
  prop(ref) { propertySlider(ref, range, name = ref.name) }

fun BaseSketch.doubleProp(ref: KMutableProperty0<Double>, range: DoubleRange) =
  prop(ref) { propertySlider(ref, range, name = ref.name) }

fun BaseSketch.doublePairProp(
  ref: KMutableProperty0<Point>,
  range: DoubleRange
) = doublePairProp(ref, range to range)

fun BaseSketch.doublePairProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0)
) = prop(ref) {
  ControlGroup(*propertySliderPair(ref, ranges.first, ranges.second, ref.name))
}

fun BaseSketch.noiseProp(
  ref: KMutableProperty0<Noise>
) = prop(ref) { noiseControls(ref).toControlSection() }

fun dropdownList(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  onChange: (String) -> Unit = {}
) = Control.Dropdown(
  text = name,
  options = options,
  defaultValue = ref.get()
) {
  ref.set(it)
  onChange(it)
}

fun <E : Enum<E>> BaseSketch.enumProp(
  ref: KMutableProperty0<E>,
  onChange: () -> Unit = {},
) = prop(ref) { propertyEnumDropdown(ref, name = ref.name, onChange = { onChange() }) }

fun <E : Enum<E>> BaseSketch.nullableEnumProp(
  ref: KMutableProperty0<E?>,
  values: Array<E>,
  onChange: () -> Unit = {},
) = prop(ref) {
  val noneOption = "None"
  Control.Dropdown(
    text = ref.name,
    options = listOf(noneOption) + values.map { it.name },
    defaultValue = ref.get()?.name ?: noneOption
  ) { selectedOption ->
    val newValue =
      if (selectedOption == noneOption) null
      else values.find { it.name == selectedOption }

    ref.set(newValue)
    markDirty()
    onChange()
  }
}

