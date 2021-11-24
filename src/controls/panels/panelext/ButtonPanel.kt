package controls.panels.panelext

import BaseSketch
import controls.controlsealedclasses.Control.Button
import controls.panels.ControlStyle
import controls.panels.PanelBuilder

fun PanelBuilder.button(
  text: String,
  style: ControlStyle? = null,
  onClick: BaseSketch.() -> Unit,
) = addNewPanel(style) { Button(text, onClick) }
