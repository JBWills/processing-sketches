package controls

import BaseSketch
import controlP5.ControlP5
import controlP5.Controller
import controlP5.DropdownList
import controlP5.Tab
import controlP5.Textfield
import controls.panels.ControlItem
import controls.panels.ControlPanel
import controls.panels.Panelable
import controls.utils.selectFile
import coordinate.BoundRect
import coordinate.Point
import util.DoubleRange
import util.PointRange
import util.bounds
import util.image.ImageCrop
import util.image.pasteOnTopCentered
import util.image.scaleAndCrop
import util.image.solidColorPImage
import util.io.loadImageMemo
import util.io.noImageSelectedFilepath
import util.positionAndSize
import util.range
import util.splitCamelCase
import util.style
import util.toDoubleRange
import util.xRange
import util.yRange
import java.awt.Color
import java.io.File
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
  val block: T.(BaseSketch) -> Unit,
) : Panelable {
  override fun toControlPanel(): ControlPanel = ControlItem(control = this)

  var ref: T? = null

  var refValue: Float?
    get() = ref?.value
    set(value) {
      if (value != null && value != ref?.value) ref?.value = value
    }

  fun applyToControl(
    sketch: BaseSketch,
    controlP5: ControlP5,
    tab: Tab,
    panel: ControlPanel,
    bound: BoundRect
  ) {
    ref = controlP5.createFunc(panel.id).apply {
      label = this@Control.name
      moveTo(tab)
      positionAndSize(bound)
      style(panel.styleFromParents)

      block(sketch)
    }
  }

  class ImageFile(
    fieldName: String,
    defaultPath: String = "",
    thumbnailCrop: ImageCrop = ImageCrop.Fill,
    onChange: BaseSketch.(String?) -> Unit,
  ) : Control<controlP5.Button>(
    fieldName,
    ControlP5::addButton,
    { sketch ->
      fun updateThumbnailAndLabel(path: String) {
        val noImageSelected =
          path.isBlank() || !File(path).exists() || path == noImageSelectedFilepath

        sketch.loadImageMemo(if (noImageSelected) noImageSelectedFilepath else path)
          ?.scaleAndCrop(thumbnailCrop, bounds)
          ?.let { setImage(it.pasteOnTopCentered(solidColorPImage(bounds.size, Color.PINK))) }
        label = if (noImageSelected) "No Image Selected" else path
        setCaptionLabel(if (noImageSelected) "No Image Selected" else path)
        isLabelVisible = true
      }

      setColorBackground(Color.PINK.rgb)

      updateThumbnailAndLabel(defaultPath)
      onClick {
        sketch.selectFile { file ->
          val path = if (file == null || file.path == noImageSelectedFilepath) "" else file.path
          updateThumbnailAndLabel(path)
          sketch.onChange(path)
        }
      }
    },
  )

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
    handleClick: BaseSketch.() -> Unit,
  ) : Control<controlP5.Button>(
    text,
    ControlP5::addButton,
    { sketch ->
      onClick { sketch.handleClick() }
    },
  ) {
    companion object {
      fun buttonProp(
        text: String,
        onClick: BaseSketch.() -> Unit
      ) = Button(text, onClick)
    }
  }

  class Toggle(
    text: String,
    defaultValue: Boolean = false,
    handleToggled: BaseSketch.(Boolean) -> Unit,
  ) : Control<controlP5.Toggle>(
    text,
    ControlP5::addToggle,
    { sketch ->
      setValue(defaultValue)
      onChange { sketch.handleToggled(booleanValue) }
      captionLabel.align(ControlP5.CENTER, ControlP5.CENTER)
      captionLabel.setSize(13)
    },
  ) {
    constructor(
      valRef: KMutableProperty0<Boolean>,
      text: String? = null,
      handleChange: BaseSketch.(Boolean) -> Unit = {},
    ) : this(
      text?.splitCamelCase() ?: valRef.name.splitCamelCase(),
      valRef.get(),
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
    handleChange: BaseSketch.(Double) -> Unit,
  ) : Control<controlP5.Slider>(
    text,
    ControlP5::addSlider,
    { sketch ->
      range(range)

      this.value = defaultValue?.toFloat() ?: range.start.toFloat()
      onChange { sketch.handleChange(value.toDouble()) }
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
      handleChange: BaseSketch.(Double) -> Unit,
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
      handleChange: BaseSketch.(Double) -> Unit = {},
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
      handleChange: BaseSketch.(Int) -> Unit = {},
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
    handleChange: BaseSketch.(Point) -> Unit,
  ) : Control<controlP5.Slider2D>(
    text,
    ControlP5::addSlider2D,
    { sketch ->
      range(rangeX, rangeY)
      val defaultPoint = defaultValue ?: Point(rangeX.start, rangeY.start)

      cursorX = defaultPoint.x.toFloat()
      cursorY = defaultPoint.y.toFloat()

      onChange { sketch.handleChange(Point(arrayValue[0], arrayValue[1])) }
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
      handleChange: BaseSketch.(Point) -> Unit = {},
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
      handleChange: BaseSketch.(Point) -> Unit,
    ) : this(text, range.xRange, range.yRange, defaultValue, handleChange)
  }

  class Dropdown(
    text: String,
    options: List<String>,
    defaultValue: String,
    handleChange: BaseSketch.(String) -> Unit = {},
  ) : Control<DropdownList>(
    text,
    ControlP5::addDropdownList,
    { sketch ->
      setType(DropdownList.LIST)
      setItems(options)

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        sketch.handleChange(selectedOption)
      }
    },
  )

  class EnumDropdown<E : Enum<E>>(
    text: String,
    defaultValue: E,
    handleChange: BaseSketch.(E) -> Unit = {},
  ) : Control<DropdownList>(
    text,
    ControlP5::addDropdownList,
    { sketch ->
      setType(DropdownList.LIST)
      val options = defaultValue.declaringClass.enumConstants.sortedBy { it.name }
      setItems(options.map { it.name })

      value = options.indexOf(defaultValue).toFloat()

      onChange {
        val selectedOption = options[value.toInt()]
        sketch.handleChange(selectedOption)
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
}
