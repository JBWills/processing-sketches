package controls

import controlP5.ControlP5
import controlP5.DropdownList
import controlP5.Tab
import coordinate.Point
import util.DoubleRange
import util.PointRange
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
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

val DEFAULT_RANGE = 0.0..1.0

sealed class Control(
  val applyToControl: (c: ControlP5, t: Tab, pos: Position, size: Size) -> Unit,
) : ControlGroupable {
  override fun toControlGroup() = ControlGroup(this)

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
    ) : this(text?.splitCamelCase() ?: valRef.name.splitCamelCase(), valRef.get(), {
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
    ) : this(text?.splitCamelCase() ?: valRef.name.splitCamelCase(), range, valRef.get(), {
      valRef.set(it)
      handleChange(it)
    })

    constructor(
      valRef: KMutableProperty0<Int>,
      range: IntRange,
      text: String? = null,
      handleChange: (Int) -> Unit = {},
    ) : this(
      text = text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
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
    ) : this(text?.splitCamelCase() ?: valRef.name.splitCamelCase(), rangeX, rangeY, valRef.get(), {
      valRef.set(it)
      handleChange(it)
    })

    constructor(
      text: String,
      range: PointRange = Point.Zero..Point.One,
      defaultValue: Point? = null,
      handleChange: (Point) -> Unit,
    ) : this(text, range.xRange, range.yRange, defaultValue, handleChange)
  }

  class Dropdown(
    text: String,
    options: List<String>,
    defaultValue: String,
    handleChange: (String) -> Unit = {},
  ) : Control(
    dropdownWith(text.splitCamelCase()) {
      setType(DropdownList.LIST)
      setItems(options)

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        handleChange(selectedOption)
      }
    }
  )

  class EnumDropdown<E : Enum<E>>(
    text: String,
    defaultValue: E,
    handleChange: (E) -> Unit = {},
  ) : Control(
    dropdownWith(text.splitCamelCase()) {
      setType(DropdownList.LIST)
      val options = defaultValue.declaringClass.enumConstants.sortedBy { it.name }
      setItems(options.map { it.name })

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        handleChange(selectedOption)
      }
    }
  ) {
    constructor(
      enumRef: KMutableProperty0<E>,
      text: String? = null,
      handleChange: (E) -> Unit = {},
    ) : this(text?.splitCamelCase() ?: enumRef.get().declaringClass.simpleName, enumRef.get(), {
      enumRef.set(it)
      handleChange(it)
    })
  }
}
