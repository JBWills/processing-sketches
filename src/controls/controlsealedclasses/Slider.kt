package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.Slider
import controls.panels.ControlStyle
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.panels.panelext.markDirtyIf
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.RefWrapper
import controls.panels.panelext.util.wrapSelf
import controls.panels.panelext.util.wrapped
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
    valRef: RefGetter<Double>,
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

  companion object {
    @JvmName("degreeSlider")
    fun PanelBuilder.slider(
      ref: KMutableProperty0<Deg>,
      range: DoubleRange = 0.0..360.0,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ): Panelable {
      val wrapped: RefWrapper<Deg, Double> = ref.wrapped({ value }, { Deg(this) })
      return slider(
        wrapped, range,
        style = style,
        shouldMarkDirty = shouldMarkDirty,
      )
    }

    @JvmName("intSlider")
    fun PanelBuilder.slider(
      ref: KMutableProperty0<Int>,
      range: IntRange,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ): Panelable {
      val wrapped: RefWrapper<Int, Double> = ref.wrapped({ toDouble() }, { toInt() })
      return slider(
        wrapped, range.toDoubleRange(),
        style = style,
        shouldMarkDirty = shouldMarkDirty,
      )
    }

    fun PanelBuilder.slider(
      ref: KMutableProperty0<Double>,
      range: DoubleRange = ZeroToOne,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider(
      ref.wrapSelf(), range,
      style = style,
      shouldMarkDirty = shouldMarkDirty,
    )

    fun PanelBuilder.slider(
      ref: KMutableProperty0<Double>,
      range: IntRange,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider(
      ref.wrapSelf(),
      range.toDoubleRange(),
      style = style,
      shouldMarkDirty = shouldMarkDirty,
    )

    fun PanelBuilder.slider(
      ref: RefGetter<Double>,
      range: DoubleRange,
      name: String? = null,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style)
    {
      GenericProp(ref) {
        Slider(ref, range, text = name) { markDirtyIf(shouldMarkDirty) }
      }
    }
  }
}
