package sketches

import FastNoiseLite.NoiseType.ValueCubic
import appletExtensions.draw.shape
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.noisePanel
import controls.props.PropData
import coordinate.Circ
import coordinate.Point
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.UNION
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.numbers.times
import util.polylines.PolyLine
import util.polylines.clipping.ForceClosedOption.NoClose
import util.polylines.clipping.clip
import util.polylines.closed

class Flower : LayeredCanvasSketch<FlowerData, FlowerLayerData>(
  "Flower",
  FlowerData(),
  { FlowerLayerData() },
) {
  init {
    numLayers = MAX_LAYERS
  }

  var unionShape: List<PolyLine>? = null

  override fun drawSetup(layerInfo: DrawInfo) {
    unionShape = null
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val layerIndex = layerInfo.layerIndex
    val (
      clipToBounds,
      noise,
      numCircles,
      maxRad,
      minRad,
      baseNumInternalCircles,
      distBetweenNoisePerCircle,
    ) = layerInfo.globalValues

    val (
      distBetweenInternalCircles,
      numInternalCircles,
    ) = layerInfo.tabValues

    if (layerIndex > numCircles) return

    val circleNoise = Noise(
      noise,
      offset = noise.offset + (distBetweenNoisePerCircle * layerIndex),
    )

    val baseRadius = (minRad..maxRad)
      .atAmountAlong((layerIndex.toDouble() + 1) / numCircles)

    val baseCircle = Circ(center, baseRadius)

    val totalInternalCircles: Int = baseNumInternalCircles + numInternalCircles

    totalInternalCircles.times { indexInverted ->
      val innerCircleIndex = (totalInternalCircles - 1) - indexInverted
      val amountAlongInnerCircle =
        innerCircleIndex.toDouble() / totalInternalCircles

      val radius = baseRadius - (innerCircleIndex * distBetweenInternalCircles)

      if (radius < 0) return@times

      val c = Circ(center, radius)
      val warpedCircle = c.walk(circleNoise.quality.step) {
        val originalPoint = baseCircle.pointAtAngle(c.angleAtPoint(it))

        val movedOriginalPoint =
          circleNoise.moveRadially(originalPoint, center) { noiseVal ->
            (noiseVal) * circleNoise.strength.magnitude * (if (layerIndex == 0) 0 else 1)
          }

        return@walk it + (movedOriginalPoint - originalPoint)
      }

      val s = warpedCircle.closed()

      if (unionShape == null) {
        unionShape = listOf(s)
        shape(warpedCircle)
        return@times
      }

      val nonNullUnionShape = unionShape ?: return@times

      val sDiffed = s.clip(nonNullUnionShape, ClipType.DIFFERENCE, NoClose)

      if (amountAlongInnerCircle == 0.0) {
        unionShape = nonNullUnionShape.clip(s, UNION)
      }

      sDiffed.toList().draw(if (clipToBounds) boundRect else null)
    }
  }
}

/**
 * Data class for single flower layer
 */
@Serializable
data class FlowerLayerData(
  var distBetweenInternalCircles: Double = 10.0,
  var numInternalCircles: Int = 1,
) : PropData<FlowerLayerData> {
  override fun bind() = layerTab {
    slider(::distBetweenInternalCircles, 1.0..200.0)
    slider(::numInternalCircles, 0..200)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

/**
 * Data class for global flower data
 */
@Serializable
data class FlowerData(
  var clipToBounds: Boolean = false,
  var noise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0),
  ),
  var numCircles: Int = 10,
  var maxRad: Double = 300.0,
  var minRad: Double = 30.0,
  var baseNumInternalCircles: Int = 1,
  var distBetweenNoisePerCircle: Double = 150.0,
) : PropData<FlowerData> {
  override fun bind() = singleTab("Flower") {
    toggle(::clipToBounds)
    slider(::numCircles, 1..LayeredCanvasSketch.MAX_LAYERS)
    row {
      slider(::maxRad, 100.0..2000.0)
      slider(::minRad, 0.0..400.0)
    }
    slider(::baseNumInternalCircles, 1..100)
    slider(::distBetweenNoisePerCircle, 0.0..150.0)

    noisePanel(::noise)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

fun main() = Flower().run()
