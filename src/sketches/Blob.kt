package sketches

import appletExtensions.getParallelLinesInBound
import controls.panels.ControlList.Companion.row
import controls.panels.ControlStyle.Companion.Green
import controls.panels.ControlStyle.Companion.Red
import controls.panels.ControlTab.Companion.layerTab
import controls.panels.ControlTab.Companion.singleTab
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
import util.algorithms.contouring.walkThreshold
import util.atAmountAlong
import util.geomutil.intersection

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
    val (noise, gridStep, thresholdStart, thresholdEnd, numThresholds) = values.tabValues
    val (shape, lineAngle, lineNoise, lineDensity, lineOffset) = values.globalValues

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)
    val lines = getParallelLinesInBound(
      boundRect.expand(lineNoise.strength.x, lineNoise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines,
    )

    val cPath = shape.getRPath(boundRect)

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
  var thresholdStart: Double = 0.4,
  var thresholdEnd: Double = 0.9,
  var numThresholds: Int = 1,
) : PropData<BlobLayerData> {
  override fun bind() = layerTab {
    slider(::gridStep, 1.0..30.0, style = Green)

    noisePanel(::noise, showStrengthSliders = false)

    row {
      style = Red
      slider(::thresholdStart, ZeroToOne)
      slider(::thresholdEnd, ZeroToOne)
    }

    intSlider(::numThresholds, 1..10)
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
