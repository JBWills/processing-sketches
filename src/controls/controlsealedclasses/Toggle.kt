package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.Toggle
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import util.splitCamelCase
import kotlin.reflect.KMutableProperty0

class Toggle(
  text: String,
  defaultValue: Boolean = false,
  handleToggled: BaseSketch.(Boolean) -> Unit,
) : Control<Toggle>(
  text,
  ControlP5::addToggle,
  { sketch, _ ->
    setValue(defaultValue)
    onChange { sketch.handleToggled(booleanValue) }
    captionLabel.align(ControlP5.CENTER, ControlP5.CENTER)
  },
) {
  constructor(
    valRef: KMutableProperty0<Boolean>,
    text: String? = null,
    handleChange: BaseSketch.(Boolean) -> Unit = {},
  ) : this(
    text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
    valRef.get(),
    {
      valRef.set(it)
      handleChange(it)
    },
  )

  companion object {
    fun PanelBuilder.toggle(
      ref: KMutableProperty0<Boolean>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) { Toggle(ref, text = ref.name) { markDirtyIf(shouldMarkDirty) } }
    }
  }
}
