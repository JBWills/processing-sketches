package controls.props.types

import controls.Control.Slider
import controls.props.GenericProp.Companion.prop
import coordinate.Deg
import util.DoubleRange
import kotlin.reflect.KMutableProperty0

fun degProp(ref: KMutableProperty0<Deg>, range: DoubleRange = 0.0..360.0) =
  prop(ref) {
    Slider(ref.name, range, ref.get().value) {
      ref.set(Deg(it))
      markDirty()
    }
  }
