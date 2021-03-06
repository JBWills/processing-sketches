package sketches

import FastNoiseLite.NoiseType.Perlin
import appletExtensions.getParallelLinesInBound
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.degreeSlider
import controls.panels.panelext.dropdown
import controls.panels.panelext.noisePanel
import controls.panels.panelext.slider
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import geomerativefork.src.RShape.Companion.createEllipse
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
  { MoireLinesLayerData() },
) {
  init {
    numLayers = 2
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (
      shape,
      lineDensity,
      lineAngle,
      lineOffset,
      shapeSize,
      shapeCenter,
      shapeRotation,
      noise
    ) = layerInfo.tabValues

    val centerPoint = boundRect.pointAt(shapeCenter.x, shapeCenter.y)

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)

    val baseLines = getParallelLinesInBound(
      boundRect.expand(noise.strength.x, noise.strength.y),
      lineAngle,
      distanceBetweenLines,
      lineOffset * distanceBetweenLines,
    )

    when (shape) {
      Rectangle -> {
        val r = boundRect.scale(shapeSize, centerPoint)

        baseLines.flatMap { it.warped(noise).intersection(r) }
      }
      Circle -> {
        val cPath = createEllipse(
          centerPoint.toRPoint(),
          (shapeSize * (boundRect.bottomRight - boundRect.topLeft)).toRPoint(),
        ).also {
          it.rotate(shapeRotation.value.toFloat(), centerPoint.toRPoint())
          it.polygonize()
        }.paths.first()
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
  var shapeRotation: Deg = Deg(0),
  var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0),
  ),
) : PropData<MoireLinesLayerData> {
  override fun bind() = layerTab {
    dropdown(::shape)
    row {
      slider(::lineDensity, 0.5..1.0)
      degreeSlider(::lineAngle, 0.0..90.0)
    }
    slider(::lineOffset, 0.0..1.0)
    sliderPair(::shapeSize, 0.0..2.0, withLockToggle = true, defaultLocked = true)
    sliderPair(::shapeCenter, -0.5..1.5)
    degreeSlider(::shapeRotation, 0.0..90.0)
    noisePanel(::noise)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MoireLinesData(
  var lineDensity: Double = 0.5,
) : PropData<MoireLinesData> {
  override fun bind() = singleTab("Global") {

  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MoireLines().run()
