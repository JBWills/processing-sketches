package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.DropdownList
import controls.utils.setupDDList
import util.splitCamelCase
import kotlin.reflect.KMutableProperty0

class EnumDropdown<E : Enum<E>>(
  text: String,
  defaultValue: E,
  handleChange: BaseSketch.(E) -> Unit = {},
) : Control<DropdownList>(
  text,
  ControlP5::addDropdownList,
  { sketch, style ->
    val options = defaultValue.declaringClass.enumConstants.sortedBy { it.name }
    setupDDList(sketch, style, defaultValue.name, options.map { it.name }) {
      handleChange(options[value.toInt()])
    }
  },
) {
  constructor(
    enumRef: KMutableProperty0<E>,
    text: String? = null,
    handleChange: BaseSketch.(E) -> Unit = {},
  ) : this(
    text?.splitCamelCase() ?: enumRef.get().declaringClass.simpleName,
    enumRef.get(),
    {
      enumRef.set(it)
      handleChange(it)
    },
  )
}
