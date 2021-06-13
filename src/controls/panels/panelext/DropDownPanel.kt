package controls.panels.panelext

import BaseSketch
import controls.Control.Dropdown
import controls.Control.EnumDropdown
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import interfaces.NamedObject
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.dropdown(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
  onChange: BaseSketch.(String) -> Unit = {},
) = addNewPanel(style) {
  Dropdown(text = name, options = options, defaultValue = ref.get()) {
    ref.set(it)
    onChange(it)
    markDirtyIf(shouldMarkDirty)
  }
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

fun <E : NamedObject> PanelBuilder.listDropdown(
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
