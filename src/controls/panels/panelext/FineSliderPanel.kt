package controls.panels.panelext

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.wrapSelf
import controls.props.GenericProp
import util.base.DoubleRange
import kotlin.reflect.KMutableProperty0

data class FineSliderArgs(
  val range: DoubleRange,
  val fineRange: DoubleRange,
  val style: ControlStyle? = null,
  val shouldMarkDirty: Boolean = true,
  val name: String? = null,
)

fun PanelBuilder.fineSlider(
  ref: KMutableProperty0<Double>,
  args: FineSliderArgs? = null
) = fineSliderPanel(ref.wrapSelf(), args)

fun PanelBuilder.fineSliderPanel(
  ref: RefGetter<Double>,
  args: FineSliderArgs? = null,
): Panelable {
  val (range, fineRange, style, shouldMarkDirty, name) = args
    ?: FineSliderArgs(0.0..10.0, 0.0..1.0)
  return addNewPanel(style) {
    var coarseGetter: RefGetter<Double>? = null

    val fineGetter: RefGetter<Double> = object : RefGetter<Double> {
      var fine: Double = 0.0
      override val name: String = "${ref.name}_fine"
      override fun get(): Double = fine
      override fun set(item: Double) {
        fine = item
        ref.set(fine + (coarseGetter?.get() ?: 0.0))
      }
    }

    coarseGetter = object : RefGetter<Double> {
      var coarse: Double = ref.get()
      override val name: String = "${ref.name}_coarse"
      override fun get(): Double = coarse
      override fun set(item: Double) {
        coarse = item
        ref.set(fineGetter.get() + coarse)
      }
    }

    GenericProp(ref) {
      col {
        slider(coarseGetter, range = range, shouldMarkDirty = shouldMarkDirty, name = name)
        slider(fineGetter, range = fineRange, shouldMarkDirty = shouldMarkDirty, name = name)
      }
    }
  }
}


