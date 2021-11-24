package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.Controller
import controlP5.DropdownList
import controlP5.Tab
import controlP5.Textfield
import controls.panels.ControlItem
import controls.panels.ControlPanel
import controls.panels.ControlStyle
import controls.panels.LabelAlign
import controls.panels.LabelAlign.Companion.align
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.LabelAlignHorizontal.Left
import controls.panels.LabelAlignVertical.Center
import controls.panels.LabelAlignVertical.Top
import controls.panels.Panelable
import controls.utils.selectFile
import controls.utils.setupDDList
import coordinate.BoundRect
import coordinate.Point
import util.PointRange
import util.base.DoubleRange
import util.bounds
import util.image.ImageCrop
import util.image.pimage.pasteOnTopCentered
import util.image.pimage.scaleAndCrop
import util.image.pimage.solidColorPImage
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
  val block: T.(BaseSketch, ControlStyle) -> Unit,
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

      block(sketch, panel.styleFromParents)
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
    { sketch, _ ->
      fun updateThumbnailAndLabel(path: String) {
        val noImageSelected =
          path.isBlank() || !File(path).exists() || path == noImageSelectedFilepath

        sketch.loadImageMemo(if (noImageSelected) noImageSelectedFilepath else path)
          ?.scaleAndCrop(thumbnailCrop, bounds)
          ?.let { setImage(it.pasteOnTopCentered(solidColorPImage(bounds.size, Color.PINK))) }
        label = fieldName
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

  class FileName(
    fieldName: String,
    defaultPath: String?,
    onChange: BaseSketch.(String?) -> Unit,
  ) : Control<controlP5.Button>(
    fieldName,
    ControlP5::addButton,
    { sketch, _ ->
      fun updateLabel(path: String?) {
        val noFileSelected = path == null || path.isEmpty()
        label = fieldName
        setCaptionLabel(if (noFileSelected) "Select File" else path)
        isLabelVisible = true
      }

      updateLabel(defaultPath)
      onClick {
        sketch.selectFile { file ->
          val path = file?.path
          updateLabel(path)
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
    { _, _ ->
      setValue(defaultValue)

      alignCaptionAndLabel(
        valueAlign = LabelAlign(Left, Center),
        captionAlign = LabelAlign(Left, Top),
      )
    },
  )

  class Button(
    text: String,
    handleClick: BaseSketch.() -> Unit,
  ) : Control<controlP5.Button>(
    text,
    ControlP5::addButton,
    { sketch, _ ->
      onClick { sketch.handleClick() }
      captionLabel.align(LabelAlign.Centered)
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
    { sketch, _ ->
      setValue(defaultValue)
      onChange { sketch.handleToggled(booleanValue) }
      captionLabel.align(ControlP5.CENTER, ControlP5.CENTER)
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
    { sketch, style ->
      range(range)

      this.value = defaultValue?.toFloat() ?: range.start.toFloat()
      onChange { sketch.handleChange(value.toDouble()) }

      // For some reason we have to do this here even though we're already doing it
      alignCaptionAndLabel(valueAlign = style.labelAlign, captionAlign = style.captionAlign)
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
    { sketch, _ ->
      range(rangeX, rangeY)
      val defaultPoint = defaultValue ?: Point(rangeX.start, rangeY.start)

      cursorX = defaultPoint.x.toFloat()
      cursorY = defaultPoint.y.toFloat()

      onChange { sketch.handleChange(Point(arrayValue[0], arrayValue[1])) }
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
    { sketch, style -> setupDDList(sketch, style, defaultValue, options, handleChange) },
  )

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
}
