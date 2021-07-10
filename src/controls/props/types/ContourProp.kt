package controls.props.types

import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.slider
import controls.panels.panelext.sliderPair
import controls.panels.panelext.toggle
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import coordinate.Segment
import kotlinx.serialization.Serializable
import util.algorithms.contouring.getContour
import util.debugLog
import util.interpolation.interpolate
import util.pow
import util.select
import util.toRange
import util.until


@Serializable
data class ContourProp(
  var thresholdRange: Pair<Double, Double> = 0.4 to 0.9,
  var numThresholds: Int = 1,
  var thresholdEaseInOut: Pair<Double, Double> = 1.0 to 1.0,
  var gridStep: Double = 5.0,
  var shouldSmooth: Boolean = false,
  var smoothEpsilon: Double = 3.0,
  var chaikinTimes: Int = 0,
) : PropData<ContourProp> {
  constructor(
    base: ContourProp,
    thresholdRange: Pair<Double, Double>? = null,
    numThresholds: Int? = null,
    thresholdEaseInOut: Pair<Double, Double>? = null,
    gridStep: Double? = null,
    shouldSmooth: Boolean? = null,
    smoothEpsilon: Double? = null,
    chaikinTimes: Int? = null,
  ) : this(
    thresholdRange = thresholdRange ?: base.thresholdRange,
    numThresholds = numThresholds ?: base.numThresholds,
    thresholdEaseInOut = thresholdEaseInOut ?: base.thresholdEaseInOut,
    gridStep = gridStep ?: base.gridStep,
    shouldSmooth = shouldSmooth ?: base.shouldSmooth,
    smoothEpsilon = smoothEpsilon ?: base.smoothEpsilon,
    chaikinTimes = chaikinTimes ?: base.chaikinTimes,
  )

  fun getThresholds(): List<Double> = thresholdRange
    .toRange()
    .select(numThresholds) { t ->
      (t.pow(thresholdEaseInOut.first)..(1 - (1 - t).pow(thresholdEaseInOut.second)))
        .interpolate(t)
    }

  fun contour(
    bounds: BoundRect,
    gridStepOverride: Double = gridStep,
    thresholds: List<Double> = listOf(),
    vF: (Point) -> Double
  ): Map<Double, List<Segment>> = getContour(
    thresholds.also { debugLog(it) },
    gridStepOverride,
    bounds.left until bounds.right,
    bounds.top until bounds.bottom,
  ) { x, y -> vF(Point(x, y)) }

  override fun toSerializer() = serializer()

  override fun clone() = ContourProp(this)

  override fun bind(): List<ControlTab> = singleTab(this::class.simpleName!!) {
    sliderPair(::thresholdRange, range = 0.0..5000.0)
    slider(::numThresholds, 1..100)

    sliderPair(::thresholdEaseInOut, range = -1.0..5.0)

    row {
      style = ControlStyle.Red

      toggle(::shouldSmooth)

      slider(::gridStep, 1..5)
      slider(::smoothEpsilon, 0.25..2.0)
      slider(::chaikinTimes, 0..5)
    }
  }
}
