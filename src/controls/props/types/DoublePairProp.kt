package controls.props.types

import controls.Control.Slider
import controls.Control.Toggle
import controls.panels.ControlList.Companion.row
import controls.props.GenericProp.Companion.prop
import coordinate.Point
import util.DoubleRange
import util.tuple.and
import kotlin.reflect.KMutableProperty0

fun doublePairProp(
  ref: KMutableProperty0<Point>,
  range: DoubleRange,
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
) = doublePairProp(ref, range to range, withLockToggle, defaultLocked)

fun doublePairProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
) = prop(ref) {
  var locked: Boolean = defaultLocked && ref.get().x == ref.get().y
  var ctrlY: Slider? = null
  val ctrlX = Slider(
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

  val ctrlToggle = Toggle(
    text = "Lock ${ref.name}",
    defaultValue = locked,
  ) {
    locked = it
    markDirty()
  }.withWidth(0.5)

  row(ref.name) {
    +ctrlX
    +ctrlY
    if (withLockToggle) +ctrlToggle
  }
}
