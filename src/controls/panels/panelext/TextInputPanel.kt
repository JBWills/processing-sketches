package controls.panels.panelext

import BaseSketch
import controls.Control.Button
import controls.Control.TextInput
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.textInput(
  textFieldLabel: String,
  submitButtonLabel: String,
  style: ControlStyle = ControlStyle.EmptyStyle,
  onSubmit: BaseSketch.(String) -> Unit,
) = addNewPanel(style) {
  val input = TextInput(textFieldLabel)
  val button = Button.buttonProp(submitButtonLabel) {
    val text = input.ref?.text ?: return@buttonProp
    onSubmit(text)
  }

  row {
    +input
    +button.withWidth(0.5)
  }
}

fun PanelBuilder.textInput(
  textField: KMutableProperty0<String>,
  style: ControlStyle = ControlStyle.EmptyStyle,
  onChange: BaseSketch.(String) -> Unit = {},
) = addNewPanel(style) {
  row {
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
