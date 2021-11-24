package controls.props.types

import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import coordinate.Segment
import kotlinx.serialization.Serializable
import util.algorithms.contouring.getContour
import util.base.select
import util.base.toRange
import util.interpolation.interpolate
import util.iterators.times
import util.numbers.pow

@Serializable
data class ContourProp(
  var thresholdRange: Pair<Double, Double> = 0.4 to 0.9,
  var numThresholds: Int = 1,
  var thresholdEaseInOut: Pair<Double, Double> = 1.0 to 1.0,
  var gridStep: Double = 5.0,
  var chaikinTimes: Int = 0,
  var simplifier: LineSimplifierProp = LineSimplifierProp()
) : PropData<ContourProp> {
  constructor(
    base: ContourProp,
    thresholdRange: Pair<Double, Double>? = null,
    numThresholds: Int? = null,
    thresholdEaseInOut: Pair<Double, Double>? = null,
    gridStep: Double? = null,
    chaikinTimes: Int? = null,
  ) : this(
    thresholdRange = thresholdRange ?: base.thresholdRange,
    numThresholds = numThresholds ?: base.numThresholds,
    thresholdEaseInOut = thresholdEaseInOut ?: base.thresholdEaseInOut,
    gridStep = gridStep ?: base.gridStep,
    chaikinTimes = chaikinTimes ?: base.chaikinTimes,
  )

  fun getThresholds(multiplier: Double = 1.0): List<Double> = thresholdRange
    .toRange()
    .select(numThresholds) { t ->
      (t.pow(thresholdEaseInOut.first)..(1 - (1 - t).pow(thresholdEaseInOut.second)))
        .interpolate(t)
    }.times(multiplier)

  fun contour(
    bounds: BoundRect,
    gridStepOverride: Double = gridStep,
    thresholds: List<Double> = listOf(),
    vF: (Point) -> Double
  ): Map<Double, List<Segment>> = getContour(thresholds, gridStepOverride, bounds) { x, y ->
    vF(Point(x, y))
  }

  override fun toSerializer() = serializer()

  override fun clone() = ContourProp(this)

  override fun bind(): List<ControlTab> = singleTab(this::class.simpleName!!) {
    sliderPair(::thresholdRange, range = 0.0..1.0)
    slider(::numThresholds, 1..100)

    sliderPair(::thresholdEaseInOut, range = -1.0..5.0)

    row {
      style = ControlStyle.Red

      slider(::gridStep, 1..5)
      slider(::chaikinTimes, 0..5)
    }

    panel(::simplifier)
  }
}
