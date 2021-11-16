package controls.utils

import BaseSketch
import controlP5.DropdownList
import controls.panels.ControlStyle
import controls.panels.LabelAlign
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.LabelAlignHorizontal.Left
import controls.panels.LabelAlignVertical.Center

fun DropdownList.setupDDList(
  sketch: BaseSketch,
  style: ControlStyle,
  defaultValue: String,
  options: List<String>,
  handleChange: BaseSketch.(String) -> Unit = {},
) {
  setType(DropdownList.LIST)
  setItems(options)
  setItemHeight(style.font.baseSize.toInt() + 18)
  barHeight = style.font.baseSize.toInt() + 28

  value = options.indexOf(defaultValue).toFloat()

  alignCaptionAndLabel(LabelAlign(Left, Center), LabelAlign(Left, Center))

  onChange {
    val selectedOption = options[value.toInt()]
    sketch.handleChange(selectedOption)
  }
}
