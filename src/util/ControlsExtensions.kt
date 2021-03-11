package util

import BaseSketch
import controlP5.Button
import controlP5.ControlP5
import controlP5.Controller
import controlP5.DropdownList
import controlP5.Slider
import controlP5.Slider2D
import controlP5.Tab
import controlP5.Textfield
import controlP5.Toggle
import controls.Control
import controls.Control.TextInput
import controls.ControlGroup
import controls.DEFAULT_RANGE
import coordinate.PixelPoint
import coordinate.Point
import kotlin.reflect.KMutableProperty0

typealias Size = PixelPoint
typealias Position = Point

private fun getId(text: String, tab: Tab) = (text + tab.name).replace(" ", "")

fun textInputWithSubmit(
  textFieldLabel: String,
  submitButtonLabel: String,
  onSubmit: (String) -> Unit,
): ControlGroup {
  val input = TextInput(textFieldLabel)
  val button = button(submitButtonLabel) {
    val text = input.ref?.text ?: return@button
    onSubmit(text)
  }

  return ControlGroup(input, button)
}

fun BaseSketch.propertyToggle(
  prop: KMutableProperty0<Boolean>,
  name: String? = null,
) =
  Control.Toggle(prop, text = name) { markDirty() }

fun BaseSketch.propertySlider(
  prop: KMutableProperty0<Double>,
  r: DoubleRange = DEFAULT_RANGE,
  name: String? = null,
) = Control.Slider(prop, r, text = name) { markDirty() }

fun BaseSketch.propertySliderPair(
  prop: KMutableProperty0<Point>,
  rx: DoubleRange = DEFAULT_RANGE,
  ry: DoubleRange = DEFAULT_RANGE,
  name: String? = null,
) = arrayOf(
  Control.Slider(
    "${name ?: prop.name} X",
    range = rx,
    getter = { prop.get().x },
    setter = { prop.set(Point(it, prop.get().y)) }
  ) { markDirty() },
  Control.Slider(
    "${name ?: prop.name} Y",
    range = ry,
    getter = { prop.get().y },
    setter = { prop.set(Point(prop.get().x, it)) }
  ) { markDirty() },
)

fun <E : Enum<E>> BaseSketch.propertyEnumDropdown(
  prop: KMutableProperty0<E>,
  onChange: () -> Unit = { },
  name: String? = null,
) = Control.EnumDropdown(prop, text = name) { onChange(); markDirty() }

fun BaseSketch.propertySlider(
  prop: KMutableProperty0<Int>,
  r: IntRange,
  name: String? = null,
) = Control.Slider(prop, r, text = name) { markDirty() }

fun button(
  text: String,
  onClick: () -> Unit
) = Control.Button(text, onClick)

fun BaseSketch.property2DSlider(
  prop: KMutableProperty0<Point>,
  rx: DoubleRange = DEFAULT_RANGE,
  ry: DoubleRange = DEFAULT_RANGE,
  name: String? = null,
) = Control.Slider2d(prop, rx, ry, text = name) { markDirty() }

fun BaseSketch.property2DSlider(
  prop: KMutableProperty0<Point>,
  r: PointRange = Point(0, 0)..Point(1, 1),
  name: String? = null,
) = Control.Slider2d(prop, r.xRange, r.yRange, text = name) { markDirty() }

fun <T> Controller<T>.position(p: Position): T = setPosition(p.xf, p.yf)

fun <T> Controller<T>.size(p: Size): T = setSize(p.x, p.y)

fun Slider.range(r: DoubleRange): Slider = setRange(r.start.toFloat(), r.endInclusive.toFloat())
fun Slider2D.range(x: DoubleRange, y: DoubleRange): Slider2D =
  setMinMax(
    x.start.toFloat(), y.start.toFloat(), x.endInclusive.toFloat(),
    y.endInclusive.toFloat()
  )

fun <T> Controller<T>.positionAndSize(p: Position?, s: Size?): Controller<T> {
  whenNotNull(p) { position(it) }
  whenNotNull(s) { size(it) }
  return this
}

fun <T : Controller<T>> T.applyWithPosAndSize(
  pos: Position,
  size: Size,
  block: T.() -> Unit = {},
  tab: Tab,
  text: String,
) = apply {
  label = text
  moveTo(tab)
  positionAndSize(pos, size)
  block()
}

fun textInputWith(text: String, block: Textfield.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Textfield =
  { c, tab, pos, size ->
    c.addTextfield(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }

fun buttonWith(text: String, block: Button.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Button =
  { c, tab, pos, size ->
    c.addButton(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }

fun toggleWith(text: String, block: Toggle.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Toggle =
  { c, tab, pos, size ->
    c.addToggle(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }

fun sliderWith(text: String, block: Slider.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Slider =
  { c, tab, pos, size ->
    c.addSlider(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }

fun slider2dWith(text: String, block: Slider2D.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Slider2D =
  { c, tab, pos, size ->
    c.addSlider2D(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }

fun dropdownWith(text: String, block: DropdownList.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> DropdownList =
  { c, tab, pos, size ->
    c.addDropdownList(getId(text, tab))
      .applyWithPosAndSize(pos, size, block, tab, text)
  }
