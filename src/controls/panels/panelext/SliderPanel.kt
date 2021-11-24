package controls.panels.panelext

import controls.controlsealedclasses.Control.Slider
import controls.controlsealedclasses.Control.Slider2d
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import coordinate.Deg
import coordinate.Point
import util.PointRange
import util.base.DoubleRange
import util.base.ZeroToOne
import util.toDoubleRange
import util.tuple.and
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

@JvmName("degreeSlider")
fun PanelBuilder.slider(
  ref: KMutableProperty0<Deg>,
  range: DoubleRange = 0.0..360.0,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) {
    Slider(ref.name, range, ref.get().value) {
      ref.set(Deg(it))
      markDirtyIf(shouldMarkDirty)
    }
  }
}

@JvmName("intSlider")
fun PanelBuilder.slider(
  ref: KMutableProperty0<Int>,
  range: IntRange,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
}

fun PanelBuilder.slider(
  ref: KMutableProperty0<Double>,
  range: DoubleRange = ZeroToOne,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) { Slider(ref, range) { markDirtyIf(shouldMarkDirty) } }
}

fun PanelBuilder.slider(
  ref: KMutableProperty0<Double>,
  range: IntRange,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = slider(ref, range.toDoubleRange(), style, shouldMarkDirty)

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
    Slider2d(ref, ranges.first, ranges.second) {
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
