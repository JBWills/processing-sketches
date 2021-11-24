package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.DropdownList
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import controls.utils.setupDDList
import interfaces.NamedObject
import kotlin.reflect.KMutableProperty0

class Dropdown(
  text: String,
  options: List<String>,
  defaultValue: String,
  handleChange: BaseSketch.(String) -> Unit = {},
) : Control<DropdownList>(
  text,
  ControlP5::addDropdownList,
  { sketch, style -> setupDDList(sketch, style, defaultValue, options, handleChange) },
) {
  companion object {
    fun PanelBuilder.dropdown(
      name: String,
      options: List<String>,
      initialValue: String,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(String) -> Unit = {},
    ) = addNewPanel(style) {
      Dropdown(text = name, options = options, defaultValue = initialValue) {
        onChange(it)
        markDirtyIf(shouldMarkDirty)
      }
    }

    fun PanelBuilder.dropdown(
      name: String,
      options: List<String>,
      ref: KMutableProperty0<String>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(String) -> Unit = {},
    ) = dropdown(name, options, ref.get(), style, shouldMarkDirty) {
      ref.set(it)
      onChange(it)
    }

    fun <E : Enum<E>> PanelBuilder.dropdown(
      ref: KMutableProperty0<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.() -> Unit = {},
    ) = addNewPanel(style) {
      GenericProp(ref) {
        EnumDropdown(ref, text = ref.name) { onChange(); markDirtyIf(shouldMarkDirty) }
      }
    }

    fun <E : Enum<E>> PanelBuilder.dropdown(
      ref: KMutableProperty0<E?>,
      values: Array<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.() -> Unit = {},
    ) = addNewPanel(style) {
      GenericProp(ref) {
        val noneOption = "None"
        Dropdown(
          text = ref.name,
          options = listOf(noneOption) + values.map { it.name },
          defaultValue = ref.get()?.name ?: noneOption,
        ) { selectedOption ->
          val newValue =
            if (selectedOption == noneOption) null
            else values.find { it.name == selectedOption }

          ref.set(newValue)
          onChange()
          markDirtyIf(shouldMarkDirty)
        }
      }
    }

    fun <E : NamedObject> PanelBuilder.dropdown(
      ref: KMutableProperty0<E>,
      values: List<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.() -> Unit = {},
    ) = addNewPanel(style) {
      GenericProp(ref) {
        Dropdown(
          text = ref.name,
          options = values.map { it.name },
          defaultValue = ref.get().name,
        ) { selectedOption ->
          val newValue = values.find { it.name == selectedOption } ?: ref.get()

          ref.set(newValue)
          onChange()
          markDirtyIf(shouldMarkDirty)
        }
      }
    }
  }
}
