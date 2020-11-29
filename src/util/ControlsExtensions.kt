package util

import BaseSketch
import SketchConfig
import controlP5.Button
import controlP5.ControlP5
import controlP5.Controller
import controlP5.Slider
import controlP5.Slider2D
import controlP5.Toggle
import controls.Control
import controls.DEFAULT_RANGE
import coordinate.PixelPoint
import coordinate.Point
import kotlin.reflect.KMutableProperty0

typealias Size = PixelPoint
typealias Position = Point

fun <TConfig : SketchConfig> BaseSketch<TConfig>.propertyToggle(prop: KMutableProperty0<Boolean>) =
  Control.Toggle(prop) { markDirty() }

fun <TConfig : SketchConfig> BaseSketch<TConfig>.propertyDoubleToggle(prop: KMutableProperty0<Boolean>, prop2: KMutableProperty0<Boolean>) =
  Control.DoubleToggle(prop, prop2, { markDirty() }, { markDirty() })

fun <TConfig : SketchConfig> BaseSketch<TConfig>.propertySlider(
  prop: KMutableProperty0<Double>,
  r: DoubleRange = DEFAULT_RANGE,
) = Control.Slider(prop, r) { markDirty() }

fun <TConfig : SketchConfig> BaseSketch<TConfig>.propertySlider(
  prop: KMutableProperty0<Int>,
  r: IntRange,
) = Control.Slider(prop, r) { markDirty() }

fun <TConfig : SketchConfig> BaseSketch<TConfig>.property2DSlider(
  prop: KMutableProperty0<Point>,
  rx: DoubleRange = DEFAULT_RANGE,
  ry: DoubleRange = DEFAULT_RANGE,
) = Control.Slider2d(prop, rx, ry) { markDirty() }

fun <TConfig : SketchConfig> BaseSketch<TConfig>.property2DSlider(
  prop: KMutableProperty0<Point>,
  r: PointRange = Point(0, 0)..Point(1, 1),
) = Control.Slider2d(prop, r.xRange, r.yRange) { markDirty() }

fun <T> Controller<T>.position(p: Position): T = setPosition(p.xf, p.yf)

fun <T> Controller<T>.size(p: Size): T = setSize(p.x, p.y)

fun Slider.range(r: DoubleRange): Slider = setRange(r.start.toFloat(), r.endInclusive.toFloat())
fun Slider2D.range(x: DoubleRange, y: DoubleRange): Slider2D =
  setMinMax(x.start.toFloat(), y.start.toFloat(), x.endInclusive.toFloat(), y.endInclusive.toFloat())

fun <T> Controller<T>.positionAndSize(p: Position?, s: Size?): Controller<T> {
  whenNotNull(p) { position(it) }
  whenNotNull(s) { size(it) }
  return this
}

fun <T : Controller<T>> T.applyWithPosAndSize(pos: Position, size: Size, block: T.() -> Unit = {}) {
  positionAndSize(pos, size)
  block()
}

fun buttonWith(text: String, block: Button.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size -> c.addButton(text).applyWithPosAndSize(pos, size, block) }

fun toggleWith(text: String, block: Toggle.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size -> c.addToggle(text).applyWithPosAndSize(pos, size, block) }

fun doubleToggleWith(text1: String, text2: String, block: Toggle.() -> Unit = {}, block2: Toggle.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size ->
    val midPoint = Point(pos.x + (size.x / 2), pos.y)
    val halfWidth = (size.toPoint() / Point(2, 1)).toPixelPoint()
    c.addToggle(text1).applyWithPosAndSize(pos, halfWidth, block)
    c.addToggle(text2).applyWithPosAndSize(midPoint, halfWidth, block2)
  }

fun sliderWith(text: String, block: Slider.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size -> c.addSlider(text).applyWithPosAndSize(pos, size, block) }

fun slider2dWith(text: String, block: Slider2D.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size -> c.addSlider2D(text).applyWithPosAndSize(pos, size, block) }