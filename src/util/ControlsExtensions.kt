package util

import controlP5.Button
import controlP5.ControlP5
import controlP5.Controller
import controlP5.DropdownList
import controlP5.Slider
import controlP5.Slider2D
import controlP5.Tab
import controlP5.Textfield
import controlP5.Toggle
import controls.Control.Button.Companion.button
import controls.Control.TextInput
import controls.panels.ControlList
import controls.panels.ControlList.Companion.row
import controls.panels.ControlPanel
import coordinate.BoundRect

typealias BindFunc<T> = (ControlP5, Tab, ControlPanel, bound: BoundRect) -> T

fun textInput(
  textFieldLabel: String,
  submitButtonLabel: String,
  onSubmit: (String) -> Unit,
): ControlList {
  val input = TextInput(textFieldLabel)
  val button = button(submitButtonLabel) {
    val text = input.ref?.text ?: return@button
    onSubmit(text)
  }

  return row(input, button)
}

fun Slider.range(r: DoubleRange): Slider = setRange(r.start.toFloat(), r.endInclusive.toFloat())
fun Slider2D.range(x: DoubleRange, y: DoubleRange): Slider2D =
  setMinMax(
    x.start.toFloat(),
    y.start.toFloat(),
    x.endInclusive.toFloat(),
    y.endInclusive.toFloat(),
  )

fun <T> Controller<T>.positionAndSize(bound: BoundRect): Controller<T> {
  bound.topLeft.let { setPosition(it.xf, it.yf) }
  bound.size.toPixelPoint().let { setSize(it.x, it.y) }
  return this
}

fun <T : Controller<T>> getBindFunc(
  text: String,
  cf: ControlP5.(id: String) -> T,
  block: T.() -> Unit = {}
): BindFunc<T> = { c, tab, panel, bound ->
  cf(c, panel.id).apply {
    label = text
    moveTo(tab)
    positionAndSize(bound)
    panel.styleFromParents.let {
      setColorBackground(it.backgroundColor.rgb)
      setColorActive(it.onHoverColor.rgb)
      setColorForeground(it.color.rgb)
      setColorLabel(it.textColor.rgb)
    }

    block()
  }
}

fun textInputWith(text: String, block: Textfield.() -> Unit = {}) =
  getBindFunc<Textfield>(text, ControlP5::addTextfield, block)

fun buttonWith(text: String, block: Button.() -> Unit = {}) =
  getBindFunc<Button>(text, ControlP5::addButton, block)

fun toggleWith(text: String, block: Toggle.() -> Unit = {}) =
  getBindFunc<Toggle>(text, ControlP5::addToggle, block)

fun sliderWith(text: String, block: Slider.() -> Unit = {}) =
  getBindFunc<Slider>(text, ControlP5::addSlider, block)

fun slider2dWith(text: String, block: Slider2D.() -> Unit = {}) =
  getBindFunc<Slider2D>(text, ControlP5::addSlider2D, block)

fun dropdownWith(text: String, block: DropdownList.() -> Unit = {}) =
  getBindFunc<DropdownList>(text, ControlP5::addDropdownList, block)
