package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.markDirtyIf
import controls.props.GenericProp
import coordinate.Point
import util.PointRange
import util.base.DoubleRange
import util.range
import util.splitCamelCase
import util.toDoubleRange
import util.tuple.and
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

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

  constructor(
    text: String,
    range: PointRange = Point.Zero..Point.One,
    defaultValue: Point? = null,
    handleChange: BaseSketch.(Point) -> Unit,
  ) : this(text, range.xRange, range.yRange, defaultValue, handleChange)

  companion object {
    @JvmName("slider2DWithSingleDoubleRange")
    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      range: DoubleRange = 0.0..1.0,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider2D(ref, range and range, style, shouldMarkDirty)

    @JvmName("slider2DWithSingleIntRange")
    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      range: IntRange = 0..1,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider2D(ref, range and range, style, shouldMarkDirty)

    @JvmName("slider2DWithIntRange")
    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      ranges: Pair<IntRange, IntRange> = (0..1) and (0..1),
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider2D(
      ref,
      ranges.first.toDoubleRange() and ranges.second.toDoubleRange(),
      style,
      shouldMarkDirty,
    )

    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = addNewPanel(style) {
      GenericProp(ref) {
        Slider2D(ref, ranges.first, ranges.second) {
          markDirtyIf(shouldMarkDirty)
        }
      }
    }

    fun PanelBuilder.slider2D(
      ref: KMutableProperty0<Point>,
      range: PointRange = Point.Zero..Point.One,
      style: ControlStyle? = null,
      shouldMarkDirty: Boolean = true,
    ) = slider2D(ref, ranges = range.xRange and range.yRange, style, shouldMarkDirty)
  }
}
