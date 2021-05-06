package sketches

import appletExtensions.getParallelLinesInBound
import controls.panels.ControlStyle.Companion.Green
import controls.panels.ControlStyle.Companion.Red
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import controls.props.types.ShapeProp
import controls.props.types.ShapeType.Rectangle
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.algorithms.chaikin
import util.algorithms.contouring.getNoiseContour
import util.algorithms.contouring.mergeSegments
import util.algorithms.contouring.walkThreshold
import util.algorithms.douglassPeucker
import util.atAmountAlong
import util.geomutil.intersection
import util.numSteps

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Blob : LayeredCanvasSketch<BlobData, BlobLayerData>(
  "Blob",
  BlobData(),
  { BlobLayerData() },
) {
  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (
      noise,
      gridStep,
      drawBlob,
      drawDebug,
      thresholdStart,
      thresholdEnd,
      numThresholds,
      shouldSmooth,
      smoothEpsilon,
      chaikinTimes,
    ) = values.tabValues

    val (
      shape,
      lineAngle,
      lineNoise,
      lineDensity,
      lineOffset,
    ) = values.globalValues
//
//    boundRect.shrink(30).asPolyLine().chaikin(chaikinTimes).draw()
//    return

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)
    val lines = getParallelLinesInBound(
      boundRect.expand(lineNoise.strength.x, lineNoise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines,
    )

    val cPath = shape.getRPath(boundRect)

    if (drawBlob) {
      getNoiseContour(
        (thresholdStart..thresholdEnd numSteps numThresholds).toList(),
        boundRect,
        gridStep,
        noise,
      ).map { (threshold, line) ->
        var mergedLines = line.mergeSegments().toList()

        if (shouldSmooth) {
          mergedLines = mergedLines.map { it.chaikin(chaikinTimes).douglassPeucker(smoothEpsilon) }
        }

        mergedLines.draw(drawDebug)
      }
    }

    lines.flatMap {
      it.warped(lineNoise)
        .walkThreshold(noise, thresholdStart)
        .flatMap { lineSegment -> lineSegment.intersection(cPath) }
    }.draw()
  }
}

@Serializable
data class BlobLayerData(
  var noise: Noise = Noise.DEFAULT.with(
    scale = 1.07,
  ),
  var gridStep: Double = 1.0,
  var drawBlob: Boolean = false,
  var drawDebug: Boolean = false,
  var thresholdStart: Double = 0.4,
  var thresholdEnd: Double = 0.9,
  var numThresholds: Int = 1,
  var shouldSmooth: Boolean = false,
  var smoothEpsilon: Double = 3.0,
  var chaikinTimes: Int = 0,
) : PropData<BlobLayerData> {
  override fun bind() = layerTab {
    row {
      toggle(::drawBlob)
      toggle(::drawDebug)
    }

    slider(::gridStep, 1.0..30.0, style = Green)

    noisePanel(::noise, showStrengthSliders = false)

    row {
      style = Red
      slider(::thresholdStart, ZeroToOne)
      slider(::thresholdEnd, ZeroToOne)
    }

    intSlider(::numThresholds, 1..20)

    row {
      style = Red

      toggle(::shouldSmooth)
      slider(::smoothEpsilon, 0.25..20.0)
      intSlider(::chaikinTimes, 0..5)
    }
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class BlobData(
  var shape: ShapeProp = ShapeProp(
    type = Rectangle,
    size = Point.One * 200,
    center = Point.Half,
    rotation = Deg(0),
  ),
  var lineAngle: Deg = Deg(45),
  var lineNoise: Noise = Noise.DEFAULT,
  var lineDensity: Double = 0.3,
  var lineOffset: Double = 0.0,
) : PropData<BlobData> {
  override fun bind() = singleTab("Global") {
    panel(::shape, style = Red)
    slider(::lineDensity, ZeroToOne)
    slider(::lineOffset, ZeroToOne)
    degreeSlider(::lineAngle)
    noisePanel(::lineNoise)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Blob().run()
