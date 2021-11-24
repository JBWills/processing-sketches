package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controls.panels.ControlStyle
import controls.panels.LabelAlign
import controls.panels.LabelAlign.Companion.align
import controls.panels.PanelBuilder

class Button(
  text: String,
  handleClick: BaseSketch.() -> Unit,
) : Control<controlP5.Button>(
  text,
  ControlP5::addButton,
  { sketch, _ ->
    onClick { sketch.handleClick() }
    captionLabel.align(LabelAlign.Centered)
  },
) {
  companion object {
    fun PanelBuilder.button(
      text: String,
      style: ControlStyle? = null,
      onClick: BaseSketch.() -> Unit,
    ) = addNewPanel(style) { Button(text, onClick) }
  }
}
