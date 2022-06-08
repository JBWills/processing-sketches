package controls.controlsealedclasses

import BaseSketch
import controlP5.ColorPicker
import controlP5.ControlP5
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import util.splitCamelCase
import java.awt.Color
import kotlin.reflect.KMutableProperty0

/**
 * Hacky thing we have to do because colorpicker doesn't have a decent onChange.
 */
class ColorPlugClass(val onChange: () -> Unit) {
  fun onColorChangedMustKeepNameConsistent(i: Int) {
    onChange()
  }
}

class ColorPicker(
  text: String,
  defaultValue: Color = Color.WHITE,
  onChange: BaseSketch.(Color) -> Unit
) : Control<ColorPicker>(
  text,
  ControlP5::addColorPicker,
  { sketch, _ ->
    colorValue = defaultValue.rgb
    val colorPlug = ColorPlugClass {
      sketch.onChange(Color(colorValue))
    }
    plugTo(colorPlug, "onColorChangedMustKeepNameConsistent")
    captionLabel.align(ControlP5.CENTER, ControlP5.CENTER)
  },
) {
  constructor(
    valRef: KMutableProperty0<Color>,
    text: String? = null,
    handleChange: BaseSketch.(Color) -> Unit = {},
  ) : this(
    text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
    valRef.get(),
    {
      valRef.set(it)
      handleChange(it)
    },
  )

  companion object {
    fun PanelBuilder.colorPicker(
      ref: KMutableProperty0<Color>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) { ColorPicker(ref, text = ref.name) { markDirtyIf(shouldMarkDirty) } }
    }
  }
}
