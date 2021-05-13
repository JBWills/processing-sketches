package controls.panels.panelext

import BaseSketch
import controls.Control.Button
import controls.Control.TextInput
import controls.panels.ControlList
import controls.panels.ControlStyle
import controls.panels.PanelBuilder

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

  ControlList.row {
    +input
    +button.withWidth(0.5)
  }
}
