package controls.props.types

import BaseSketch
import controls.controlsealedclasses.Control.Button
import controls.controlsealedclasses.Control.TextInput
import controls.panels.ControlList
import controls.panels.ControlList.Companion.row

fun textInputProp(
  textFieldLabel: String,
  submitButtonLabel: String,
  onSubmit: BaseSketch.(String) -> Unit,
): ControlList {
  val input = TextInput(textFieldLabel)
  val button = Button.buttonProp(submitButtonLabel) {
    val text = input.ref?.text ?: return@buttonProp
    onSubmit(text)
  }

  return row {
    +input
    +button.withWidth(0.5)
  }
}
