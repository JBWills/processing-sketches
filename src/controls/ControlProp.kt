package controls

import BaseSketch
import controls.Control.Dropdown
import controls.Control.EnumDropdown
import controls.Control.Slider2d
import controls.ControlGroup.Companion.group
import controls.ControlSection.Companion.toControlSection
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import util.DoubleRange
import util.ZeroToOne
import util.propertySlider
import util.propertyToggle
import util.toDoubleRange
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

fun BaseSketch.doubleProp(ref: KMutableProperty0<Double>, range: DoubleRange = ZeroToOne) =
  prop(ref) { propertySlider(ref, range, name = ref.name) }

fun BaseSketch.doubleProp(ref: KMutableProperty0<Double>, range: IntRange) =
  doubleProp(ref, range.toDoubleRange())

fun BaseSketch.doublePairProp(
  ref: KMutableProperty0<Point>,
  range: DoubleRange,
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
) = doublePairProp(ref, range to range, withLockToggle, defaultLocked)

fun BaseSketch.doublePairProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
) = prop(ref) {
  var locked: Boolean = defaultLocked && ref.get().x == ref.get().y
  var ctrlY: Control.Slider? = null
  val ctrlX: Control.Slider = Control.Slider(
    "${ref.name} X",
    range = ranges.first,
    getter = { ref.get().x },
    setter = {
      ref.set(Point(it, ref.get().y))
      if (locked) ctrlY?.refValue = it.toFloat()
    }
  ) { markDirty() }

  ctrlY = Control.Slider(
    "${ref.name} Y",
    range = ranges.second,
    getter = { ref.get().y },
    setter = {
      ref.set(Point(ref.get().x, it))
      if (locked) ctrlX.refValue = it.toFloat()
    }
  ) { markDirty() }

  val ctrlToggle: Control.Toggle = Control.Toggle(
    text = "Lock ${ref.name}",
    defaultValue = locked,
  ) {
    locked = it
    markDirty()
  }

  group(
    ctrlX,
    ctrlY,
    if (withLockToggle) ctrlToggle else null
  )
}

fun BaseSketch.pointProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0)
) = prop(ref) {
  Slider2d(ref, ranges.first, ranges.second, text = ref.name) { markDirty() }
}

fun BaseSketch.noiseProp(
  ref: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
) = prop(ref) { noiseControls(ref, showStrengthSliders).toControlSection() }

fun dropdownList(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  onChange: (String) -> Unit = {}
) = Dropdown(
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
) = prop(ref) {
  EnumDropdown(ref, text = ref.name) { onChange(); markDirty() }
}

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

fun BaseSketch.degProp(ref: KMutableProperty0<Deg>, range: DoubleRange = 0.0..360.0) =
  prop(ref) {
    Control.Slider(ref.name, range, ref.get().value) {
      ref.set(Deg(it))
      markDirty()
    }
  }

