package util

import BaseSketch
import controlP5.*
import controls.Control
import controls.DEFAULT_RANGE
import coordinate.PixelPoint
import coordinate.Point
import kotlin.reflect.KMutableProperty0

typealias Size = PixelPoint
typealias Position = Point

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
  Control.Slider(prop.get().mutablePropX, rx, "${name ?: prop.name} X") { markDirty() },
  Control.Slider(prop.get().mutablePropY, ry, "${name ?: prop.name} Y") { markDirty() }
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
  setMinMax(x.start.toFloat(), y.start.toFloat(), x.endInclusive.toFloat(),
    y.endInclusive.toFloat())

fun <T> Controller<T>.positionAndSize(p: Position?, s: Size?): Controller<T> {
  whenNotNull(p) { position(it) }
  whenNotNull(s) { size(it) }
  return this
}

fun <T : Controller<T>> applyWithPosAndSize(
  t: T, pos: Position, size: Size, block: T.() -> Unit = {}, tab: Tab, label: String,
) = t.with {
  setLabel(label)
  moveTo(tab)
  positionAndSize(pos, size)
  block()
}

fun buttonWith(text: String, block: Button.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    applyWithPosAndSize(c.addButton(text + tab.name), pos, size, block, tab, text)
  }

fun toggleWith(text: String, block: Toggle.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    applyWithPosAndSize(c.addToggle(text + tab.name), pos, size, block, tab, text)
  }

fun doubleToggleWith(
  text1: String, text2: String, block: Toggle.() -> Unit = {}, block2: Toggle.() -> Unit = {},
)
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    val midPoint = Point(pos.x + (size.x / 2), pos.y)
    val halfWidth = (size.toPoint() / Point(2, 1)).toPixelPoint()
    applyWithPosAndSize(c.addToggle(text1 + tab.name), pos, halfWidth, block, tab, text1)
    applyWithPosAndSize(c.addToggle(text2 + tab.name), midPoint, halfWidth, block2, tab, text2)
  }

fun sliderWith(text: String, block: Slider.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    applyWithPosAndSize(c.addSlider(text + tab.name), pos, size, block, tab, text)
  }

fun slider2dWith(text: String, block: Slider2D.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    applyWithPosAndSize(c.addSlider2D(text + tab.name), pos, size, block, tab, text)
  }

fun dropdownWith(text: String, block: DropdownList.() -> Unit = {})
  : (ControlP5, Tab, pos: Position, size: Size) -> Unit =
  { c, tab, pos, size ->
    applyWithPosAndSize(c.addDropdownList(text + tab.name), pos, size, block, tab, text)
  }
