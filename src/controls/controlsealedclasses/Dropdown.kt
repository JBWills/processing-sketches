package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.DropdownList
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.panels.panelext.markDirtyIf
import controls.utils.setupDDList
import interfaces.NamedObject
import util.generics.getValues
import util.iterators.flipMap
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

    fun <T> PanelBuilder.dropdown(
      name: String,
      options: List<T>,
      initialValue: T,
      getName: (value: T) -> String,
      onSetValue: BaseSketch.(value: T) -> Unit,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(String, T) -> Unit = { _, _ -> },
    ): Panelable {
      val defaultName = getName(options.first())
      val valuesToName: Map<T, String> = options.associateBy({ it }, { getName(it) })
      val nameToValues = valuesToName.flipMap()
      val indexToNames = options.mapIndexed { index, t -> index to getName(t) }.toMap()

      return dropdown(
        name = name,
        options = options.indices.map { indexToNames.getOrDefault(it, defaultName) },
        initialValue = getName(initialValue),
        style = style,
        shouldMarkDirty = shouldMarkDirty,
        onChange = { nameString ->
          val value = nameToValues[nameString]
            ?: throw Exception("No value exists for that name: $nameString")
          onSetValue(value)
          onChange(nameString, value)
        },
      )
    }

    fun <T> PanelBuilder.dropdown(
      ref: KMutableProperty0<T>,
      name: String = ref.name,
      options: List<T>,
      getName: (value: T) -> String,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(String, T) -> Unit = { _, _ -> },
    ): Panelable = dropdown(
      name = name,
      options = options,
      initialValue = ref.get(),
      getName = getName,
      onSetValue = { v -> ref.set(v) },
      style = style,
      shouldMarkDirty = shouldMarkDirty,
      onChange = onChange,
    )

    fun PanelBuilder.dropdown(
      name: String,
      options: List<String>,
      ref: KMutableProperty0<String>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(String) -> Unit = {},
    ) = dropdown(ref, name, options, { it }, style, shouldMarkDirty) { _, v -> onChange(v) }

    fun <E : Enum<E>> PanelBuilder.dropdown(
      ref: KMutableProperty0<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(E) -> Unit = {},
    ) = dropdown(
      ref = ref,
      options = ref.getValues(),
      getName = { it.name },
      style = style,
      shouldMarkDirty = shouldMarkDirty,
    ) { _, v -> onChange(v) }

    fun <E : Enum<E>> PanelBuilder.dropdown(
      ref: KMutableProperty0<E?>,
      values: Array<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(E?) -> Unit = {},
    ) = dropdown(
      ref = ref,
      options = listOf(null, *values),
      getName = { it?.name ?: "None" },
      style = style,
      shouldMarkDirty = shouldMarkDirty,
    ) { _, v -> onChange(v) }

    fun <E : NamedObject> PanelBuilder.dropdown(
      ref: KMutableProperty0<E>,
      values: List<E>,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
      onChange: BaseSketch.(E) -> Unit = {},
    ) = dropdown(
      ref = ref,
      options = values,
      getName = { it.name },
      style = style,
      shouldMarkDirty = shouldMarkDirty,
    ) { _, v -> onChange(v) }
  }
}
