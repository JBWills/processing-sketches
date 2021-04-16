package controls

import controlP5.ControlP5
import controlP5.Controller
import controlP5.DropdownList
import controlP5.Tab
import controlP5.Textfield
import controls.panels.ControlItem
import controls.panels.ControlPanel
import controls.panels.ControlStyle
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Point
import util.DoubleRange
import util.PointRange
import util.positionAndSize
import util.range
import util.splitCamelCase
import util.style
import util.toDoubleRange
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

val DEFAULT_RANGE = 0.0..1.0

/**
 * A control binds data to ControlP5 Sliders, toggles, etc.
 *
 * @param T The ControlP5 Controller type this Control binds to
 * @property name The visible name for the control (also used to generate IDs for the control)
 *  This name must be unique within a given [ControlPanel], but a control can have the same name
 *  as another control as long as it's in a different [ControlPanel] or [ControlTab]
 * @property createFunc
 * @property block
 */
sealed class Control<T : Controller<T>>(
  var name: String,
  val createFunc: ControlP5.(id: String) -> T,
  val block: T.() -> Unit,
) : Panelable {
  override fun toControlPanel(): ControlPanel = ControlItem(control = this)

  var ref: T? = null

  var refValue: Float?
    get() = ref?.value
    set(value) {
      if (value != null && value != ref?.value) ref?.value = value
    }

  fun applyToControl(controlP5: ControlP5, tab: Tab, panel: ControlPanel, bound: BoundRect) {
    ref = createFunc(controlP5, panel.id).apply {
      label = this@Control.name
      moveTo(tab)
      positionAndSize(bound)
      style(panel.styleFromParents)

      block()
    }
  }

  class TextInput(
    fieldName: String,
    defaultValue: String = "",
  ) : Control<Textfield>(
    fieldName,
    ControlP5::addTextfield,
    { setValue(defaultValue) },
  )

  class Button(
    text: String,
    handleClick: () -> Unit,
  ) : Control<controlP5.Button>(
    text,
    ControlP5::addButton,
    {
      onClick { handleClick() }
    },
  ) {
    companion object {
      fun buttonProp(
        text: String,
        onClick: () -> Unit
      ) = Button(text, onClick)

      fun MutableList<Panelable>.button(
        text: String,
        style: ControlStyle? = null,
        onClick: () -> Unit
      ) = add(Button(text, onClick).applyStyleOverrides(style))
    }
  }

  class Toggle(
    text: String,
    defaultValue: Boolean = false,
    handleToggled: (Boolean) -> Unit,
  ) : Control<controlP5.Toggle>(
    text,
    ControlP5::addToggle,
    {
      setValue(defaultValue)
      onChange { handleToggled(booleanValue) }
      captionLabel.align(ControlP5.CENTER, ControlP5.CENTER)
      captionLabel.setSize(13)
    },
  ) {
    constructor(
      valRef: KMutableProperty0<Boolean>,
      text: String? = null,
      handleChange: (Boolean) -> Unit = {},
    ) : this(
      text?.splitCamelCase() ?: valRef.name.splitCamelCase(), valRef.get(),
      {
        valRef.set(it)
        handleChange(it)
      },
    )
  }

  class Slider(
    text: String,
    range: DoubleRange = DEFAULT_RANGE,
    defaultValue: Double? = null,
    handleChange: (Double) -> Unit,
  ) : Control<controlP5.Slider>(
    text,
    ControlP5::addSlider,
    {
      range(range)

      this.value = defaultValue?.toFloat() ?: range.start.toFloat()
      onChange { handleChange(value.toDouble()) }
      captionLabel.align(ControlP5.RIGHT, ControlP5.BOTTOM)
      captionLabel.setSize(13)
      valueLabel.align(ControlP5.LEFT, ControlP5.BOTTOM)
      valueLabel.setSize(13)
    },
  ) {
    constructor(
      text: String,
      range: DoubleRange = DEFAULT_RANGE,
      getter: () -> Double,
      setter: (Double) -> Unit,
      handleChange: (Double) -> Unit,
    ) : this(
      text, range, getter(),
      {
        setter(it)
        handleChange(it)
      },
    )

    constructor(
      valRef: KMutableProperty0<Double>,
      range: DoubleRange = DEFAULT_RANGE,
      text: String? = null,
      handleChange: (Double) -> Unit = {},
    ) : this(
      text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
      range,
      valRef::get,
      valRef::set,
      handleChange,
    )

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
      },
    )
  }

  class Slider2d(
    text: String,
    rangeX: DoubleRange = DEFAULT_RANGE,
    rangeY: DoubleRange = DEFAULT_RANGE,
    defaultValue: Point? = null,
    handleChange: (Point) -> Unit,
  ) : Control<controlP5.Slider2D>(
    text,
    ControlP5::addSlider2D,
    {
      range(rangeX, rangeY)
      val defaultPoint = defaultValue ?: Point(rangeX.start, rangeY.start)

      cursorX = defaultPoint.x.toFloat()
      cursorY = defaultPoint.y.toFloat()

      onChange { handleChange(Point(arrayValue[0], arrayValue[1])) }
      captionLabel.align(ControlP5.RIGHT, ControlP5.BOTTOM)
      captionLabel.setSize(13)
      valueLabel.align(ControlP5.LEFT, ControlP5.BOTTOM)
      valueLabel.setSize(13)
    },
  ) {
    constructor(
      valRef: KMutableProperty0<Point>,
      rangeX: DoubleRange = DEFAULT_RANGE,
      rangeY: DoubleRange = DEFAULT_RANGE,
      text: String? = null,
      handleChange: (Point) -> Unit = {},
    ) : this(
      text?.splitCamelCase() ?: valRef.name.splitCamelCase(), rangeX, rangeY, valRef.get(),
      {
        valRef.set(it)
        handleChange(it)
      },
    )

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
  ) : Control<DropdownList>(
    text,
    ControlP5::addDropdownList,
    {
      setType(DropdownList.LIST)
      setItems(options)

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        handleChange(selectedOption)
      }
    },
  )

  class EnumDropdown<E : Enum<E>>(
    text: String,
    defaultValue: E,
    handleChange: (E) -> Unit = {},
  ) : Control<DropdownList>(
    text,
    ControlP5::addDropdownList,
    {
      setType(DropdownList.LIST)
      val options = defaultValue.declaringClass.enumConstants.sortedBy { it.name }
      setItems(options.map { it.name })

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        handleChange(selectedOption)
      }
    },
  ) {
    constructor(
      enumRef: KMutableProperty0<E>,
      text: String? = null,
      handleChange: (E) -> Unit = {},
    ) : this(
      text?.splitCamelCase() ?: enumRef.get().declaringClass.simpleName, enumRef.get(),
      {
        enumRef.set(it)
        handleChange(it)
      },
    )
  }
}
