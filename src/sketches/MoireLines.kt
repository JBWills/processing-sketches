package sketches

import FastNoiseLite.NoiseType.Perlin
import appletExtensions.getParallelLinesInBound
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.noisePanel
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.MoireShape.Circle
import sketches.MoireShape.Rectangle
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.polylines.clipping.clip

enum class MoireShape {
  Rectangle,
  Circle,
}

/**
 * Create a Moiré pattern interaction between two shapes
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
      noise,
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

        baseLines.flatMap {
          it.warped(noise).clip(r.toPolyLine(), INTERSECTION)
        }
      }
      Circle -> {
        val cPath = Circ(
          centerPoint,
          (shapeSize * (boundRect.bottomRight - boundRect.topLeft)).x,
        )
          .toPolyLine()
        baseLines.flatMap { it.warped(noise).clip(cPath, INTERSECTION) }
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
    strength = Point(0, 0),
  ),
) : PropData<MoireLinesLayerData> {
  override fun bind() = layerTab {
    dropdown(::shape)
    row {
      slider(::lineDensity, 0.5..1.0)
      slider(::lineAngle, 0.0..90.0)
    }
    slider(::lineOffset, 0.0..1.0)
    sliderPair(
      ::shapeSize,
      0.0..2.0,
      withLockToggle = true,
      defaultLocked = true,
    )
    sliderPair(::shapeCenter, -0.5..1.5)
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
