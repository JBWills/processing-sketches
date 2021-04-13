package sketches

import BaseSketch
import appletExtensions.getParallelLinesInBound
import controls.degProp
import controls.doublePairProp
import controls.doubleProp
import controls.intProp
import controls.noiseProp
import controls.panels.ControlList.Companion.row
import controls.panels.ControlStyle
import controls.panels.ControlTab.Companion.layerTab
import controls.panels.ControlTab.Companion.singleTab
import controls.prop
import controls.props.PropData
import controls.props.ShapeProp
import controls.props.ShapeType.Rectangle
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import geomerativefork.src.RShape
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
    val (shape, noise, gridStep, thresholdStart, thresholdEnd, numThresholds) = values.tabValues
    val (circleCenter, circleSize, lineAngle, lineNoise, lineDensity, lineOffset) = values.globalValues

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)
    val lines = getParallelLinesInBound(
      boundRect.expand(lineNoise.strength.x, lineNoise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines,
    )

    val cPath = RShape.createCircle(
      boundRect.pointAt(circleCenter.x, circleCenter.y).toRPoint(),
      circleSize,
    ).also { it.polygonize() }.paths.first()

    lines.flatMap {
      it.warped(lineNoise)
        .walkThreshold(noise, thresholdStart)
        .flatMap { lineSegment -> lineSegment.intersection(cPath) }
    }.draw()
  }
}

@Serializable
data class BlobLayerData(
  var shape: ShapeProp = ShapeProp(
    type = Rectangle,
    size = Point.One * 200,
    center = Point.Half,
    rotation = Deg(0),
  ),
  var noise: Noise = Noise.DEFAULT.with(
    scale = 1.07,
  ),
  var gridStep: Double = 1.0,
  var thresholdStart: Double = 0.4,
  var thresholdEnd: Double = 0.9,
  var numThresholds: Int = 1,
) : PropData<BlobLayerData> {
  override fun BaseSketch.bind() = layerTab(
    prop(::shape),
    doubleProp(::gridStep, 1.0..30.0).withStyle(ControlStyle.GREEN),
    noiseProp(::noise, showStrengthSliders = false),
    row(
      doubleProp(::thresholdStart, ZeroToOne),
      doubleProp(::thresholdEnd, ZeroToOne),
    ).withStyle(ControlStyle.RED),
    intProp(::numThresholds, 1..10),
  )

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class BlobData(
  var circleCenter: Point = Point.Half,
  var circleSize: Double = 400.0,
  var lineAngle: Deg = Deg(45),
  var lineNoise: Noise = Noise.DEFAULT,
  var lineDensity: Double = 0.3,
  var lineOffset: Double = 0.0,
) : PropData<BlobData> {
  override fun BaseSketch.bind() = singleTab(
    "Global",
    doublePairProp(::circleCenter, ZeroToOne to ZeroToOne),
    doubleProp(::circleSize, 0..2000),
    doubleProp(::lineDensity, ZeroToOne),
    doubleProp(::lineOffset, ZeroToOne),
    degProp(::lineAngle),
    noiseProp(::lineNoise),
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Blob().run()
