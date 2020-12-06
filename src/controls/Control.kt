package controls

import controlP5.ControlP5
import controlP5.DropdownList
import coordinate.Point
import util.DoubleRange
import util.Position
import util.Size
import util.buttonWith
import util.dropdownWith
import util.range
import util.slider2dWith
import util.sliderWith
import util.splitCamelCase
import util.toDoubleRange
import util.toggleWith
import kotlin.reflect.KMutableProperty0

val DEFAULT_RANGE = 0.0..1.0

sealed class Control(
  val applyToControl: (c: ControlP5, pos: Position, size: Size) -> Unit,
) {
  class Button(
    text: String,
    handleClick: () -> Unit,
  ) : Control(
    buttonWith(text) {
      onClick { handleClick() }
    }
  )

  class Toggle(
    text: String,
    defaultValue: Boolean = false,
    handleToggled: (Boolean) -> Unit,
  ) : Control(
    toggleWith(text) {
      setValue(defaultValue)
      onChange { handleToggled(booleanValue) }
    }
  ) {
    constructor(
      valRef: KMutableProperty0<Boolean>,
      text: String? = null,
      handleChange: (Boolean) -> Unit = {},
    ) : this(text ?: valRef.name.splitCamelCase(), valRef.get(), {
      valRef.set(it)
      handleChange(it)
    })
  }

  class Slider(
    text: String,
    range: DoubleRange = DEFAULT_RANGE,
    defaultValue: Double? = null,
    handleChange: (Double) -> Unit,
  ) : Control(
    sliderWith(text) {
      range(range)

      this.value = defaultValue?.toFloat() ?: range.start.toFloat()
      onChange { handleChange(value.toDouble()) }
      //valueLabel.align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE)
      captionLabel.align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE)
    }
  ) {
    constructor(
      valRef: KMutableProperty0<Double>,
      range: DoubleRange = DEFAULT_RANGE,
      text: String? = null,
      handleChange: (Double) -> Unit = {},
    ) : this(text ?: valRef.name.splitCamelCase(), range, valRef.get(), {
      valRef.set(it)
      handleChange(it)
    })

    constructor(
      valRef: KMutableProperty0<Int>,
      range: IntRange,
      text: String? = null,
      handleChange: (Int) -> Unit = {},
    ) : this(
      text = text ?: valRef.name.splitCamelCase(),
      range = range.toDoubleRange(),
      defaultValue = valRef.get().toDouble(),
      handleChange =
      {
        valRef.set(it.toInt())
        handleChange(it.toInt())
      })
  }

  class Slider2d(
    text: String,
    rangeX: DoubleRange = DEFAULT_RANGE,
    rangeY: DoubleRange = DEFAULT_RANGE,
    defaultValue: Point? = null,
    handleChange: (Point) -> Unit,
  ) : Control(
    slider2dWith(text) {
      range(rangeX, rangeY)
      val defaultPoint = defaultValue ?: Point(rangeX.start, rangeY.start)

      cursorX = defaultPoint.x.toFloat()
      cursorY = defaultPoint.y.toFloat()

      onChange { handleChange(Point(arrayValue[0], arrayValue[1])) }
    }
  ) {
    constructor(
      valRef: KMutableProperty0<Point>,
      rangeX: DoubleRange = DEFAULT_RANGE,
      rangeY: DoubleRange = DEFAULT_RANGE,
      text: String? = null,
      handleChange: (Point) -> Unit = {},
    ) : this(text ?: valRef.name.splitCamelCase(), rangeX, rangeY, valRef.get(), {
      valRef.set(it)
      handleChange(it)
    })
  }

  class Dropdown<E : Enum<E>>(
    enumRef: KMutableProperty0<E>,
    text: String? = null,
    handleChange: (E) -> Unit = {},
  ) : Control(
    dropdownWith(text ?: enumRef.get().declaringClass.simpleName) {
      setType(DropdownList.LIST)
      val options = enumRef.get().declaringClass.enumConstants.sortedBy { it.name }
      setItems(options.map { it.name })

      value = options.indexOf(enumRef.get()).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        enumRef.set(selectedOption)
        handleChange(selectedOption)
      }
    }
  )
}