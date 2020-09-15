package util

import controlP5.Button
import controlP5.ControlP5
import controlP5.Controller
import controlP5.Slider
import controlP5.Toggle
import coordinate.PixelPoint
import coordinate.Point

typealias Size = PixelPoint
typealias Position = Point


fun <T> Controller<T>.position(p: Position): T = setPosition(p.x, p.y)

fun <T> Controller<T>.size(p: Size): T = setSize(p.x, p.y)

fun Slider.range(r: Pair<Float, Float>): Slider = setRange(r.first, r.second)

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

fun sliderWith(text: String, block: Slider.() -> Unit = {})
  : (ControlP5, pos: Position, size: Size) -> Unit =
  { c, pos, size -> c.addSlider(text).applyWithPosAndSize(pos, size, block) }