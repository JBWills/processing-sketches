package controls.panels.panelext

import controls.controlsealedclasses.Slider
import controls.controlsealedclasses.Toggle
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.pointWrapped
import controls.panels.panelext.util.wrapSelf
import controls.panels.panelext.util.wrapped
import controls.props.GenericProp
import coordinate.Point
import util.base.DoubleRange
import util.tuple.and
import kotlin.reflect.KMutableProperty0

@JvmName("sliderPairDoubles")
fun PanelBuilder.sliderPair(
  ref1: KMutableProperty0<Double>,
  ref2: KMutableProperty0<Double>,
  range: DoubleRange = (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = sliderPairPanel(
  pointWrapped(ref1, ref2),
  range to range,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
  xName = ref1.name,
  yName = ref2.name,
)

@JvmName("sliderPairDoubles")
fun PanelBuilder.sliderPair(
  ref1: RefGetter<Double>,
  ref2: RefGetter<Double>,
  range: DoubleRange = (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = sliderPairPanel(
  (ref1 to ref2).wrapped({ Point(first, second) }, { x to y }),
  range to range,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
  xName = ref1.name,
  yName = ref2.name,
)

@JvmName("sliderPairDoubleRange")
fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<DoubleRange>,
  range: DoubleRange = (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = sliderPairPanel(
  ref.pointWrapped(),
  range to range,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
)

@JvmName("sliderPairDoublePair")
fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<Pair<Double, Double>>,
  range: DoubleRange = (0.0..1.0),
  range2: DoubleRange = range,
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = sliderPairPanel(
  ref.pointWrapped(),
  range to range2,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
)

fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<Point>,
  range: DoubleRange,
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  shouldMarkDirty: Boolean = true,
) = sliderPair(
  ref,
  range to range,
  withLockToggle,
  defaultLocked,
  shouldMarkDirty = shouldMarkDirty,
)

fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = sliderPairPanel(
  ref.wrapSelf(),
  ranges,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
)

private fun PanelBuilder.sliderPairPanel(
  ref: RefGetter<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
  xName: String? = null,
  yName: String? = null,
) = addNewPanel(style) {
  GenericProp(ref) {
    var locked: Boolean = defaultLocked && ref.get().x == ref.get().y
    var ctrlY: Slider? = null
    val ctrlX = Slider(
      xName ?: "${ref.name} X",
      range = ranges.first,
      getter = { ref.get().x },
      setter = {
        if (locked) {
          ref.set(Point(it, it))
          ctrlY?.refValue = it.toFloat()
        } else {
          ref.set(ref.get().withX(it))
        }
      },
    ) { markDirtyIf(shouldMarkDirty) }

    ctrlY = Slider(
      yName ?: "${ref.name} Y",
      range = ranges.second,
      getter = { ref.get().y },
      setter = {
        if (locked) {
          ref.set(Point(it, it))
          ctrlX.refValue = it.toFloat()
        } else {
          ref.set(ref.get().withY(it))
        }
      },
    ) { markDirtyIf(shouldMarkDirty) }

    val ctrlToggle = Toggle(
      text = "Lock ${ref.name}",
      defaultValue = locked,
    ) {
      locked = it
      markDirtyIf(shouldMarkDirty)
    }.withWidth(0.5)

    row(name) {
      +ctrlX
      +ctrlY
      if (withLockToggle) +ctrlToggle
    }
  }
}

