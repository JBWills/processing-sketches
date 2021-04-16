package controls.props.types

import BaseSketch
import controls.Control.Slider2d
import controls.props.GenericProp.Companion.prop
import coordinate.Point
import util.DoubleRange
import util.PointRange
import util.tuple.and
import util.xRange
import util.yRange
import kotlin.reflect.KMutableProperty0

fun BaseSketch.pointProp(
  ref: KMutableProperty0<Point>,
  ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
) = prop(ref) { Slider2d(ref, ranges.first, ranges.second) { markDirty() } }

fun BaseSketch.pointProp(
  ref: KMutableProperty0<Point>,
  range: PointRange = Point.Zero..Point.One,
) = pointProp(ref, range.xRange and range.yRange)
