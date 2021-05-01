package controls.props.types

import controls.Control.Slider
import controls.Control.Toggle
import controls.props.GenericProp
import util.DoubleRange
import util.ZeroToOne
import util.toDoubleRange
import kotlin.reflect.KMutableProperty0

fun booleanProp(ref: KMutableProperty0<Boolean>) =
  GenericProp(ref) { Toggle(ref, text = ref.name) { markDirty() } }

fun intProp(ref: KMutableProperty0<Int>, range: IntRange) =
  GenericProp(ref) { Slider(ref, range) { markDirty() } }

fun doubleProp(ref: KMutableProperty0<Double>, range: DoubleRange = ZeroToOne) =
  GenericProp(ref) { Slider(ref, range) { markDirty() } }

fun doubleProp(ref: KMutableProperty0<Double>, range: IntRange) =
  doubleProp(ref, range.toDoubleRange())
