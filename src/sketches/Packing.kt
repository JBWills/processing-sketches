package sketches

import FastNoiseLite.NoiseType.ValueCubic
import appletExtensions.withStroke
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.Circ
import coordinate.Point
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.algorithms.kMeans
import util.algorithms.makeHull
import util.base.NegativeOneToOne
import util.base.letWith
import util.base.times
import util.numbers.map
import util.randomPoint
import java.awt.Color

class Packing : LayeredCanvasSketch<PackingData, PackingLayerData>(
  "Packing",
  PackingData(),
  { PackingLayerData() },
) {
  init {
    numLayers = 1
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (
      centroidNoise,
      dotNoise,
      boundDotsToCircle,
      drawDots,
      numDots,
      numCentroids,
      iterations,
      equalCardinality,
      circleSize,
      circleOffset,
    ) = layerInfo.globalValues

    randomSeed(centroidNoise.seed.toLong())

    val c = Circ(center + circleOffset, circleSize)

    val points = dotNoise
      .move(numDots.map { randomPoint(c) })
      .letWith {
        return@letWith if (boundDotsToCircle) filter { c.contains(it) } else this
      }

    val centroids = centroidNoise
      .move(numCentroids.map { randomPoint(c) })
      .letWith {
        return@letWith if (boundDotsToCircle) filter { c.contains(it) } else this
      }

    if (isDebugMode) withStroke(Color.green) {
      centroids.drawPoints()
    }

    val pointSets = points.kMeans(centroids, iterations, equalCardinality)

    pointSets.forEach { pointSet ->
      if (drawDots) pointSet.drawPoints()
      pointSet.makeHull().draw()
    }
  }
}

@Serializable
data class PackingLayerData(
  var PackingField: Int = 1,
) : PropData<PackingLayerData> {
  override fun bind() = layerTab {
    slider(::PackingField, 1..100)
  }

  override fun clone(): PackingLayerData = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class PackingData(
  var centroidNoise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(0, 0),
  ),
  var dotNoise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(0, 0),
  ),
  var boundDotsToCircle: Boolean = true,
  var drawDots: Boolean = true,
  var numDots: Int = 1000,
  var numCentroids: Int = 5,
  var iterations: Int = 32,
  var equalCardinality: Boolean = false,
  var circleSize: Double = 300.0,
  var circleOffset: Point = Point.Zero,
) : PropData<PackingData> {
  override fun bind() = singleTab(
    "Packing",
  ) {
    noisePanel(::centroidNoise)
    noisePanel(::dotNoise)
    row {
      toggle(::drawDots)
      toggle(::boundDotsToCircle)
    }
    row {
      slider(::numDots, 0..100_000)
      slider(::numCentroids, 1..10_000)
    }
    slider(::iterations, 1..32)
    toggle(::equalCardinality)
    slider(::circleSize, 50.0..800.0)
    sliderPair(::circleOffset, NegativeOneToOne * 500 to NegativeOneToOne * 500)
  }

  override fun clone(): PackingData = copy()
  override fun toSerializer() = serializer()
}

fun main() = Packing().run()
