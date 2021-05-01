package controls.props.types

import controls.Control.Dropdown
import controls.Control.EnumDropdown
import controls.props.GenericProp
import kotlin.reflect.KMutableProperty0

fun dropdownListProp(
  name: String,
  options: List<String>,
  ref: KMutableProperty0<String>,
  onChange: (String) -> Unit = {},
) = Dropdown(
  text = name,
  options = options,
  defaultValue = ref.get(),
) {
  ref.set(it)
  onChange(it)
}

fun <E : Enum<E>> enumProp(
  ref: KMutableProperty0<E>,
  onChange: () -> Unit = {},
) = GenericProp(ref) {
  EnumDropdown(ref, text = ref.name) { onChange(); markDirty() }
}

fun <E : Enum<E>> nullableEnumProp(
  ref: KMutableProperty0<E?>,
  values: Array<E>,
  onChange: () -> Unit = {},
) = GenericProp(ref) {
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
