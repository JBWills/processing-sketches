package controls.panels.panelext

import BaseSketch
import controls.controlsealedclasses.Button
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.TextInput
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.textInput(
  textFieldLabel: String,
  submitButtonLabel: String,
  style: ControlStyle = ControlStyle.EmptyStyle,
  onSubmit: BaseSketch.(String) -> Unit,
) {
  val input = TextInput(textFieldLabel)
  val button = Button(submitButtonLabel) {
    val text = input.ref?.text ?: return@Button
    onSubmit(text)
  }

  row(style = style) {
    +input
    +button.withWidth(0.5)
  }
}

fun PanelBuilder.textInput(
  textField: KMutableProperty0<String>,
  style: ControlStyle = ControlStyle.EmptyStyle,
  onChange: BaseSketch.(String) -> Unit = {},
) {
  row(style = style) {
    val textInput = TextInput(textField.name, textField.get())

    +textInput

    button("update text") {
      textInput.ref?.text?.let {
        println(it)
        textField.set(it)
        onChange(it)
        markDirty()
      }
    }
  }
}
