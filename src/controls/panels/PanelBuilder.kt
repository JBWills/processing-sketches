package controls.panels

import BaseSketch
import controls.Control.Button
import controls.Control.Slider
import controls.panels.ListDirection.Col
import controls.panels.ListDirection.Row
import controls.props.GenericProp
import controls.props.PropData
import controls.props.types.booleanProp
import controls.props.types.doublePairProp
import controls.props.types.doubleProp
import controls.props.types.dropdownListProp
import controls.props.types.enumProp
import controls.props.types.imageFileProp
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
import util.image.ImageCrop
import util.image.ImageCrop.Fill
import util.toDoubleRange
import util.tuple.and
import util.xRange
import util.yRange
import java.awt.Color
import kotlin.reflect.KMutableProperty0

open class PanelBuilder(val panels: MutableList<Panelable>) {
  var name: String? = null
  var direction: ListDirection = Col
  var style: ControlStyle? = null
  var heightRatio: Number? = null
  var widthRatio: Number? = null

  constructor(
    name: String? = null,
    direction: ListDirection? = null,
    style: ControlStyle? = null,
    heightRatio: Number? = null,
    widthRatio: Number? = null,
  ) : this(mutableListOf()) {
    this.name = name ?: this.name
    this.direction = direction ?: this.direction
    this.style = style ?: this.style
    this.heightRatio = heightRatio ?: this.heightRatio
    this.widthRatio = widthRatio ?: this.widthRatio
  }

  constructor(
    name: String? = null,
    direction: ListDirection? = null,
    style: ControlStyle? = null,
    heightRatio: Number? = null,
    widthRatio: Number? = null,
    block: PanelBuilder.() -> Unit,
  ) : this(
    name,
    direction,
    style,
    heightRatio,
    widthRatio,
  ) {
    apply(block)
  }

  operator fun Panelable.unaryPlus() = panels.add(this)
  operator fun List<Panelable>.unaryPlus() = panels.addAll(this)
  operator fun Array<Panelable>.unaryPlus() = panels.addAll(this)

  private fun Panelable.applyAndAdd(style: ControlStyle? = null) =
    applyStyleOverrides(style).also { panels.add(it) }

  fun build(): ControlList = ControlList(
    name = name ?: direction.name,
    style = style ?: ControlStyle.EmptyStyle,
    direction = direction,
    widthOverride = widthRatio?.toDouble(),
    heightOverride = heightRatio?.toDouble(),
    items = panels.toTypedArray(),
  )

  fun createAndAdd(name: String? = null, direction: ListDirection, block: PanelBuilder.() -> Unit) =
    +PanelBuilder(name, direction, block = block).build()

  fun row(name: String? = null, block: PanelBuilder.() -> Unit) = createAndAdd(name, Row, block)

  fun col(name: String? = null, block: PanelBuilder.() -> Unit) = createAndAdd(name, Col, block)

  fun button(
    text: String,
    style: ControlStyle? = null,
    onClick: BaseSketch.() -> Unit
  ) = Button(text, onClick).applyAndAdd(style)

  fun toggle(
    ref: KMutableProperty0<Boolean>,
    style: ControlStyle? = null
  ) = booleanProp(ref).applyAndAdd(style)

  fun intSlider(
    ref: KMutableProperty0<Int>, range: IntRange,
    style: ControlStyle? = null
  ) = intProp(ref, range)
    .applyAndAdd(style)

  fun slider(
    ref: KMutableProperty0<Double>,
    range: DoubleRange = ZeroToOne,
    style: ControlStyle = ControlStyle.EmptyStyle
  ) = doubleProp(ref, range)
    .applyAndAdd(style)

  fun slider(
    ref: KMutableProperty0<Double>,
    range: IntRange,
    style: ControlStyle = ControlStyle.EmptyStyle
  ) = slider(ref, range.toDoubleRange(), style)

  fun slider2D(
    ref: KMutableProperty0<Point>,
    ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
    style: ControlStyle = ControlStyle.EmptyStyle,
  ) = pointProp(ref, ranges)
    .applyAndAdd(style)

  fun slider2D(
    ref: KMutableProperty0<Point>,
    range: PointRange = Point.Zero..Point.One,
    style: ControlStyle = ControlStyle.EmptyStyle,
  ) = slider2D(ref, range.xRange and range.yRange, style)

  fun textInput(
    textFieldLabel: String,
    submitButtonLabel: String,
    style: ControlStyle = ControlStyle.EmptyStyle,
    onSubmit: BaseSketch.(String) -> Unit,
  ) = textInputProp(textFieldLabel, submitButtonLabel, onSubmit)
    .applyAndAdd(style)

  fun degreeSlider(
    ref: KMutableProperty0<Deg>,
    range: DoubleRange = 0.0..360.0,
    style: ControlStyle? = null,
  ) = GenericProp(ref) {
    Slider(ref.name, range, ref.get().value) {
      ref.set(Deg(it))
      markDirty()
    }
  }.applyAndAdd(style)

  fun sliderPair(
    ref: KMutableProperty0<Point>,
    range: DoubleRange,
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
  ) = sliderPair(ref, range to range, withLockToggle, defaultLocked, style)

  fun sliderPair(
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

  fun <E : Enum<E>> dropdownList(
    ref: KMutableProperty0<E>,
    style: ControlStyle? = null,
    onChange: () -> Unit = {},
  ) = enumProp(ref, onChange).applyAndAdd(style)

  fun <E : Enum<E>> dropdownList(
    ref: KMutableProperty0<E?>,
    values: Array<E>,
    style: ControlStyle? = null,
    onChange: () -> Unit = {},
  ) = nullableEnumProp(ref, values, onChange).applyAndAdd(style)

  fun imageSelect(
    ref: KMutableProperty0<String>,
    style: ControlStyle? = null,
    thumbnailCrop: ImageCrop = Fill,
  ) = imageFileProp(ref, thumbnailCrop).applyAndAdd(style)

  fun noisePanel(
    ref: KMutableProperty0<Noise>,
    showStrengthSliders: Boolean = true,
    style: ControlStyle = ControlStyle.Orange.withColor(
      frameBackground = Color(50, 20, 0),
    )
  ) = noiseProp(ref, showStrengthSliders).applyAndAdd(style)

  fun <T : PropData<T>> panel(
    ref: KMutableProperty0<T>,
    style: ControlStyle? = null,
  ) = GenericProp(ref) {
    ref.get().asControlPanel()
  }.applyAndAdd(style)

  companion object {
    internal fun list(block: PanelBuilder.() -> Unit): ControlList =
      PanelBuilder().apply(block).build()
  }
}
