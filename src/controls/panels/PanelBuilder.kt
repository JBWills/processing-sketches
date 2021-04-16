package controls.panels

import BaseSketch
import controls.props.GenericProp.Companion.prop
import controls.props.PropData
import controls.props.types.booleanProp
import controls.props.types.degProp
import controls.props.types.doublePairProp
import controls.props.types.doubleProp
import controls.props.types.dropdownListProp
import controls.props.types.enumProp
import controls.props.types.intProp
import controls.props.types.noiseProp
import controls.props.types.nullableEnumProp
import controls.props.types.pointProp
import controls.props.types.textInputProp
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
import java.awt.Color
import kotlin.reflect.KMutableProperty0

class PanelBuilder(val panels: MutableList<Panelable>) : MutableList<Panelable> by panels {
  constructor() : this(mutableListOf())

  operator fun Panelable.unaryPlus() = add(this)
  operator fun List<Panelable>.unaryPlus() = addAll(this)
  operator fun Array<Panelable>.unaryPlus() = addAll(this)

  private fun Panelable.applyAndAdd(style: ControlStyle? = null) =
    applyStyleOverrides(style).also { add(it) }

  fun BaseSketch.toggle(
    ref: KMutableProperty0<Boolean>,
    style: ControlStyle? = null
  ) = booleanProp(ref).applyAndAdd(style)

  fun BaseSketch.intSlider(
    ref: KMutableProperty0<Int>, range: IntRange,
    style: ControlStyle? = null
  ) =
    intProp(ref, range)
      .applyAndAdd(style)

  fun BaseSketch.slider(
    ref: KMutableProperty0<Double>,
    range: DoubleRange = ZeroToOne,
    style: ControlStyle = ControlStyle.EmptyStyle
  ) = doubleProp(ref, range)
    .applyAndAdd(style)

  fun BaseSketch.slider(
    ref: KMutableProperty0<Double>, range: IntRange,
    style: ControlStyle = ControlStyle.EmptyStyle
  ) =
    slider(ref, range.toDoubleRange(), style)

  fun BaseSketch.slider2D(
    ref: KMutableProperty0<Point>,
    ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
    style: ControlStyle = ControlStyle.EmptyStyle,
  ) = pointProp(ref, ranges)
    .applyAndAdd(style)

  fun BaseSketch.slider2D(
    ref: KMutableProperty0<Point>,
    range: PointRange = Point.Zero..Point.One,
    style: ControlStyle = ControlStyle.EmptyStyle,
  ) = slider2D(ref, range.xRange and range.yRange, style)

  fun textInput(
    textFieldLabel: String,
    submitButtonLabel: String,
    style: ControlStyle = ControlStyle.EmptyStyle,
    onSubmit: (String) -> Unit,
  ) = textInputProp(textFieldLabel, submitButtonLabel, onSubmit)
    .applyAndAdd(style)

  fun BaseSketch.degreeSlider(
    ref: KMutableProperty0<Deg>,
    range: DoubleRange = 0.0..360.0,
    style: ControlStyle? = null,
  ) = degProp(ref, range).applyAndAdd(style)

  fun BaseSketch.sliderPair(
    ref: KMutableProperty0<Point>,
    range: DoubleRange,
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
  ) = sliderPair(ref, range to range, withLockToggle, defaultLocked, style)

  fun BaseSketch.sliderPair(
    ref: KMutableProperty0<Point>,
    ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
  ) = doublePairProp(ref, ranges, withLockToggle, defaultLocked).applyAndAdd(style)

  fun dropdownList(
    name: String,
    options: List<String>,
    ref: KMutableProperty0<String>,
    style: ControlStyle? = null,
    onChange: (String) -> Unit = {},
  ) = dropdownListProp(name, options, ref, onChange).applyAndAdd(style)

  fun <E : Enum<E>> BaseSketch.dropdownList(
    ref: KMutableProperty0<E>,
    style: ControlStyle? = null,
    onChange: () -> Unit = {},
  ) = enumProp(ref, onChange).applyAndAdd(style)

  fun <E : Enum<E>> BaseSketch.dropdownList(
    ref: KMutableProperty0<E?>,
    values: Array<E>,
    style: ControlStyle? = null,
    onChange: () -> Unit = {},
  ) = nullableEnumProp(ref, values, onChange).applyAndAdd(style)

  fun BaseSketch.noisePanel(
    ref: KMutableProperty0<Noise>,
    showStrengthSliders: Boolean = true,
    style: ControlStyle = ControlStyle.Orange.withColor(
      frameBackground = Color(50, 20, 0),
    )
  ) = noiseProp(ref, showStrengthSliders, style).applyAndAdd()

  fun <T : PropData<T>> BaseSketch.panel(
    ref: KMutableProperty0<T>,
    style: ControlStyle? = null,
  ) = prop(ref).applyAndAdd(style)
}
