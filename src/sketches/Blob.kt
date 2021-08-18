package sketches

import appletExtensions.getParallelLinesInBoundMemo
import controls.panels.ControlStyle.Companion.Blue
import controls.panels.ControlStyle.Companion.Green
import controls.panels.ControlStyle.Companion.Red
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.panels.panelext.slider
import controls.panels.panelext.toggle
import controls.props.PropData
import controls.props.types.ShapeProp
import controls.props.types.ShapeType.Rectangle
import coordinate.Deg
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedMemo
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.algorithms.chaikin
import util.algorithms.contouring.getNoiseContourMemo
import util.algorithms.contouring.mergeSegments
import util.algorithms.contouring.walkThreshold
import util.algorithms.douglassPeucker
import util.atAmountAlong
import util.numSteps
import util.polylines.clipping.clipperIntersection

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

  override fun drawOnce(layerInfo: LayerInfo) {
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
    ) = layerInfo.tabValues

    val (
      shape,
      contourShape,
      lineAngle,
      lineNoise,
      lineDensity,
      lineOffset,
    ) = layerInfo.globalValues

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)
    val lines: List<Segment> = getParallelLinesInBoundMemo(
      boundRect.expand(lineNoise.strength.x, lineNoise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines,
    )

    if (drawBlob) {
      getNoiseContourMemo(
        (thresholdStart..thresholdEnd numSteps numThresholds).toList(),
        contourShape.roughBounds(boundRect),
        gridStep,
        noise,
      ).forEach { (_, lineSegments) ->
        lineSegments
          .mergeSegments()
          .map { line ->
            val smoothedLine = if (shouldSmooth)
              line.chaikin(chaikinTimes)
                .douglassPeucker(smoothEpsilon)
            else line

            smoothedLine.clipperIntersection(contourShape.getPolyLine(boundRect))
          }
          .draw()
      }
    }

    lines.flatMap {
      warpedMemo(it, lineNoise)
        .walkThreshold(noise, thresholdStart)
        .flatMap { lineSegment -> lineSegment.clipperIntersection(shape.getPolyLine(boundRect)) }
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

    slider(::numThresholds, 1..200)

    row {
      style = Red

      toggle(::shouldSmooth)
      slider(::smoothEpsilon, 0.25..20.0)
      slider(::chaikinTimes, 0..5)
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
  var contourShape: ShapeProp = ShapeProp(
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
  override fun bind() = tabs {
    tab("Crop") {
      panel(::shape, style = Red)
      panel(::contourShape, style = Blue)
    }
    tab("Global") {
      row {
        slider(::lineDensity, ZeroToOne)
        slider(::lineOffset, ZeroToOne)
      }
      slider(::lineAngle)
      noisePanel(::lineNoise)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Blob().run()
