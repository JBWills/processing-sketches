package controls

import BaseSketch
import controls.Control.Dropdown
import controls.Control.EnumDropdown
import controls.Control.Slider
import controls.Control.Slider2d
import controls.Control.Toggle
import controls.panels.ControlList.Companion.row
import controls.panels.ControlPanel
import controls.panels.ControlPanelProp
import controls.panels.Panelable
import controls.props.PropData
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import util.DoubleRange
import util.PointRange
import util.ZeroToOne
import util.toDoubleRange
import util.tuple.and
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

open class GenericReferenceField<T>(
  override val sketch: BaseSketch,
  private var ref: KMutableProperty0<T>,
  override val name: String = ref.name,
  private val controlsGetter2: () -> Panelable
) : ControlPanelProp<T> {
  override fun get(): T = ref.get()
  override fun set(newVal: T) = ref.set(newVal)

  override fun toControlPanel(): ControlPanel = controlsGetter2().toControlPanel()
}

open class ListReferenceField<T>(
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
}

fun <T> BaseSketch.prop(
  ref: KMutableProperty0<T>,
  controlsGetter2: () -> Panelable,
) = GenericReferenceField(
  this,
  ref,
  controlsGetter2 = controlsGetter2,
)

fun <T : PropData<T>> BaseSketch.prop(
  ref: KMutableProperty0<T>
) = prop(ref) { ref.get().asControlPanel(this) }

fun <T> BaseSketch.prop(
  ref: MutableList<T>,
  listIndex: Int,
  controlsGetter2: () -> Panelable,
) =
  ListReferenceField(
    this,
    ref,
    listIndex,
    name = "List Reference: $listIndex",
    controlsGetter2 = controlsGetter2,
  )

fun BaseSketch.booleanProp(ref: KMutableProperty0<Boolean>) =
  prop(ref) { Toggle(ref, text = ref.name) { markDirty() } }

fun BaseSketch.intProp(ref: KMutableProperty0<Int>, range: IntRange) =
  prop(ref) { Slider(ref, range) { markDirty() } }

fun BaseSketch.doubleProp(ref: KMutableProperty0<Double>, range: DoubleRange = ZeroToOne) =
  prop(ref) { Slider(ref, range) { markDirty() } }

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
  var ctrlY: Slider? = null
  val ctrlX: Slider = Slider(
    "${ref.name} X",
    range = ranges.first,
    getter = { ref.get().x },
    setter = {
      ref.set(Point(it, ref.get().y))
      if (locked) ctrlY?.refValue = it.toFloat()
    },
  ) { markDirty() }

  ctrlY = Slider(
    "${ref.name} Y",
    range = ranges.second,
    getter = { ref.get().y },
    setter = {
      ref.set(Point(ref.get().x, it))
      if (locked) ctrlX.refValue = it.toFloat()
    },
  ) { markDirty() }

  val ctrlToggle: Toggle = Toggle(
    text = "Lock ${ref.name}",
    defaultValue = locked,
  ) {
    locked = it
    markDirty()
  }


  row(
    name = ref.name,
    ctrlX,
    ctrlY!!,
    if (withLockToggle) ctrlToggle else null,
  )
}

fun BaseSketch.pointProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0)
) = prop(ref) { Slider2d(ref, ranges.first, ranges.second) { markDirty() } }

fun BaseSketch.pointProp(
  ref: KMutableProperty0<Point>,
  range: PointRange = Point.Zero..Point.One
) = pointProp(ref, range.xRange and range.yRange)

fun BaseSketch.noiseProp(
  ref: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
) = prop(ref) {
  noiseControls(ref, showStrengthSliders)
}

fun dropdownList(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  onChange: (String) -> Unit = {}
) = Dropdown(
  text = name,
  options = options,
  defaultValue = ref.get(),
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
  Dropdown(
    text = ref.name,
    options = listOf(noneOption) + values.map { it.name },
    defaultValue = ref.get()?.name ?: noneOption,
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
    Slider(ref.name, range, ref.get().value) {
      ref.set(Deg(it))
      markDirty()
    }
  }

