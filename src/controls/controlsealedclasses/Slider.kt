package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.Slider
import controls.panels.ControlStyle
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import coordinate.Deg
import util.base.DoubleRange
import util.base.ZeroToOne
import util.range
import util.splitCamelCase
import util.toDoubleRange
import kotlin.reflect.KMutableProperty0

class Slider(
  text: String,
  range: DoubleRange = DEFAULT_RANGE,
  defaultValue: Double? = null,
  handleChange: BaseSketch.(Double) -> Unit,
) : Control<Slider>(
  text,
  ControlP5::addSlider,
  { sketch, style ->
    range(range)

    this.value = defaultValue?.toFloat() ?: range.start.toFloat()
    onChange { sketch.handleChange(value.toDouble()) }

    // For some reason we have to do this here even though we're already doing it
    alignCaptionAndLabel(valueAlign = style.labelAlign, captionAlign = style.captionAlign)
  },
) {
  constructor(
    text: String,
    range: DoubleRange = DEFAULT_RANGE,
    getter: () -> Double,
    setter: (Double) -> Unit,
    handleChange: BaseSketch.(Double) -> Unit,
  ) : this(
    text, range, getter(),
    {
      setter(it)
      handleChange(it)
    },
  )

  constructor(
    valRef: KMutableProperty0<Double>,
    range: DoubleRange = DEFAULT_RANGE,
    text: String? = null,
    handleChange: BaseSketch.(Double) -> Unit = {},
  ) : this(
    text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
    range,
    valRef::get,
    valRef::set,
    handleChange,
  )

  constructor(
    valRef: KMutableProperty0<Int>,
    range: IntRange,
    text: String? = null,
    handleChange: BaseSketch.(Int) -> Unit = {},
  ) : this(
    text = text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
    range = range.toDoubleRange(),
    defaultValue = valRef.get().toDouble(),
    handleChange =
    {
      valRef.set(it.toInt())
      handleChange(it.toInt())
    },
  )

  companion object {
    @JvmName("degreeSlider")
    fun PanelBuilder.slider(
      ref: KMutableProperty0<Deg>,
      range: DoubleRange = 0.0..360.0,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) {
        Slider(ref.name, range, ref.get().value) {
          ref.set(Deg(it))
          markDirtyIf(shouldMarkDirty)
        }
      }
    }

    @JvmName("intSlider")
    fun PanelBuilder.slider(
      ref: KMutableProperty0<Int>,
      range: IntRange,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
    }

    fun PanelBuilder.slider(
      ref: KMutableProperty0<Double>,
      range: DoubleRange = ZeroToOne,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
    }

    fun PanelBuilder.slider(
      ref: KMutableProperty0<Double>,
      range: IntRange,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider(ref, range.toDoubleRange(), style, shouldMarkDirty)
  }
}
