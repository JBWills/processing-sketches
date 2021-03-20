package sketches

import BaseSketch
import FastNoiseLite.NoiseType.Perlin
import appletExtensions.getParallelLinesInBound
import controls.ControlTab.Companion.layerTab
import controls.ControlTab.Companion.tab
import controls.degProp
import controls.doublePairProp
import controls.doubleProp
import controls.enumProp
import controls.noiseProp
import controls.props.PropData
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.MoireShape.Circle
import sketches.MoireShape.Rectangle
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.geomutil.intersection

enum class MoireShape {
  Rectangle,
  Circle,
}

/**
 * Create a Moire pattern interaction between two shapes
 */
class MoireLines : LayeredCanvasSketch<MoireLinesData, MoireLinesLayerData>(
  "MoireLines",
  MoireLinesData(),
  { MoireLinesLayerData() }
) {
  init {
    numLayers = 2
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (baseData) = values.globalValues
    val (
      shape,
      lineDensity,
      lineAngle,
      lineOffset,
      shapeSize,
      shapeCenter,
      noise
    ) = values.tabValues

    val centerPoint = boundRect.pointAt(shapeCenter.x, shapeCenter.y)

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)

    val baseLines = getParallelLinesInBound(
      boundRect.expand(noise.strength.x, noise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines
    )

    when (shape) {
      Rectangle -> {
        val r = boundRect.scale(shapeSize, centerPoint)

        baseLines.flatMap { it.warped(noise).intersection(r) }
      }
      Circle -> {
        val c = Circ(centerPoint, shapeSize.x * boundRect.width)
        val cPath = c.toRPath().also { it.polygonize() }
        baseLines.flatMap { it.warped(noise).intersection(cPath) }
      }
    }.draw(boundRect)
  }
}

@Serializable
data class MoireLinesLayerData(
  var shape: MoireShape = Rectangle,
  var lineDensity: Double = 0.5,
  var lineAngle: Deg = Deg.VERTICAL,
  var lineOffset: Double = 0.0,
  var shapeSize: Point = Point.One,
  var shapeCenter: Point = Point.Half,
  var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0)
  ),
) : PropData<MoireLinesLayerData> {
  override fun BaseSketch.bind() = layerTab(
    enumProp(::shape),
    doubleProp(::lineDensity, 0.0..1.0),
    degProp(::lineAngle, 0.0..90.0),
    doubleProp(::lineOffset, 0.0..1.0),
    doublePairProp(::shapeSize, 0.0..2.0),
    doublePairProp(::shapeCenter, -0.5..1.5),
    noiseProp(::noise),
  )

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MoireLinesData(
  var lineDensity: Double = 0.5,
) : PropData<MoireLinesData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "Global",
    )
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MoireLines().run()
