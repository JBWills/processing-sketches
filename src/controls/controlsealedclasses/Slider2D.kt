package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import coordinate.Point
import util.base.DoubleRange
import util.range
import util.splitCamelCase
import util.toDoubleRange
import kotlin.reflect.KMutableProperty0

data class Slider2DArgs(
  val rangeX: DoubleRange,
  val rangeY: DoubleRange,
  val style: ControlStyle? = null,
  val shouldMarkDirty: Boolean = true,
  val name: String? = null,
) {
  constructor(
    ranges: Pair<DoubleRange, DoubleRange>,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    name: String? = null,
  ) :
    this(ranges.first, ranges.second, style, shouldMarkDirty, name)

  constructor(
    range: DoubleRange = 0.0..1.0,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    name: String? = null,
  ) :
    this(range, range, style, shouldMarkDirty, name)

  constructor(
    range: IntRange,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    name: String? = null,
  ) :
    this(range.toDoubleRange(), range.toDoubleRange(), style, shouldMarkDirty, name)

  constructor(
    rangeX: IntRange,
    rangeY: IntRange,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    name: String? = null,
  ) :
    this(rangeX.toDoubleRange(), rangeY.toDoubleRange(), style, shouldMarkDirty, name)
}

class Slider2D(
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

  companion object {
    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      args: Slider2DArgs? = null
    ) = slider2DPanel(ref, args)

    private fun PanelBuilder.slider2DPanel(
      ref: KMutableProperty0<Point>,
      args: Slider2DArgs? = null
    ) = addNewPanel(style) {
      val argsNonNull = args ?: Slider2DArgs()
      GenericProp(ref) {
        Slider2D(ref, argsNonNull.rangeX, argsNonNull.rangeY, text = argsNonNull.name ?: ref.name) {
          markDirtyIf(argsNonNull.shouldMarkDirty)
        }
      }
    }
  }
}
