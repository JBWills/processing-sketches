package sketches

import FastNoiseLite.NoiseType.ValueCubic
import controls.panels.ControlList.Companion.row
import controls.panels.ControlTab.Companion.layerTab
import controls.panels.ControlTab.Companion.singleTab
import controls.props.PropData
import coordinate.Point
import coordinate.Spiral
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Moire : LayeredCanvasSketch<MoireData, MoireLayerData>(
  "Moire",
  MoireData(),
  { MoireLayerData() },
) {
  init {
    numLayers = 3
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (baseData) = values.globalValues
    val (
      noise,
      numRotations,
      centerPoint,
      startRad,
      endRad,
    ) = values.tabValues

    val cp = baseData.centerPoint + centerPoint
    val spiralPoint = boundRect.pointAt(cp.x, cp.y)
    Spiral(
      originFunc = { _, _, _ ->
        spiralPoint
      },
      lengthFunc = { _, percentAlong, _ ->
        ((startRad + baseData.startRad)..(endRad + baseData.endRad)).atAmountAlong(percentAlong)
      },
      rotationsRange = 0.0..(baseData.numRotations),
    )
      .walk(noise.quality.step / 40) {
        noise.moveRadially(it, spiralPoint) { i -> i * noise.strength.magnitude }
      }
      .draw(boundRect)
  }
}

@Serializable
data class MoireLayerData(
  var noise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point(0.5, 0.5),
    strength = Point(0, 0),
  ),
  var numRotations: Double = 0.0,
  var centerPoint: Point = Point(0.0, 0.0),
  var startRad: Double = 0.0,
  var endRad: Double = 100.0,
) : PropData<MoireLayerData> {
  override fun bind() = layerTab {
    noisePanel(::noise)
    slider(::numRotations, 1.0..1000.0)
    sliderPair(::centerPoint, -1.0..1.0)
    row {
      slider(::startRad, 0.0..200.0)
      slider(::endRad, 0.0..2000.0)
    }
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MoireData(
  var baseData: MoireLayerData = MoireLayerData(
    centerPoint = Point.Half,
    numRotations = 100.0,
  ),
) : PropData<MoireData> {
  override fun bind() = singleTab("Global") {
    panel(::baseData)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Moire().run()
