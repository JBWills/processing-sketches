package controls.panels.panelext

import controls.Control.Slider
import controls.Control.Slider2d
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import coordinate.Point
import util.DoubleRange
import util.PointRange
import util.ZeroToOne
import util.toDoubleRange
import util.tuple.and
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.intSlider(
  ref: KMutableProperty0<Int>, range: IntRange,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
}

fun PanelBuilder.slider(
  ref: KMutableProperty0<Double>,
  range: DoubleRange = ZeroToOne,
  style: ControlStyle = ControlStyle.EmptyStyle,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
}

fun PanelBuilder.slider(
  ref: KMutableProperty0<Double>,
  range: IntRange,
  style: ControlStyle = ControlStyle.EmptyStyle,
  shouldMarkDirty: Boolean = true,
) = slider(ref, range.toDoubleRange(), style)

fun PanelBuilder.slider2D(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  style: ControlStyle = ControlStyle.EmptyStyle,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) { Slider2d(ref, ranges.first, ranges.second) { markDirtyIf(shouldMarkDirty) } }
}

fun PanelBuilder.slider2D(
  ref: KMutableProperty0<Point>,
  range: PointRange = Point.Zero..Point.One,
  style: ControlStyle = ControlStyle.EmptyStyle,
  shouldMarkDirty: Boolean = true,
) = slider2D(ref, ranges = range.xRange and range.yRange, style, shouldMarkDirty)
