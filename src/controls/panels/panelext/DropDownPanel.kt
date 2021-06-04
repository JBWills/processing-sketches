package controls.panels.panelext

import BaseSketch
import controls.Control.Dropdown
import controls.Control.EnumDropdown
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.dropdown(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  style: ControlStyle? = null,
  onChange: BaseSketch.(String) -> Unit = {},
) = addNewPanel(style) {
  Dropdown(text = name, options = options, defaultValue = ref.get()) {
    ref.set(it)
    onChange(it)
  }
}

fun <E : Enum<E>> PanelBuilder.dropdown(
  ref: KMutableProperty0<E>,
  style: ControlStyle? = null,
  onChange: BaseSketch.() -> Unit = {},
) = addNewPanel(style) {
  GenericProp(ref) {
    EnumDropdown(ref, text = ref.name) { onChange(); markDirty() }
  }
}

fun <E : Enum<E>> PanelBuilder.dropdown(
  ref: KMutableProperty0<E?>,
  values: Array<E>,
  style: ControlStyle? = null,
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
      markDirty()
      onChange()
    }
  }
}