package sketches

import BaseSketch
import FastNoiseLite.NoiseType.ValueCubic
import appletExtensions.withStroke
import controls.*
import controls.panels.ControlList.Companion.row
import controls.panels.ControlTab.Companion.tab
import controls.props.PropData
import coordinate.Circ
import coordinate.Point
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.*
import util.algorithms.kMeans
import util.algorithms.makeHull
import util.geomutil.toRPath
import java.awt.Color

class Packing : LayeredCanvasSketch<PackingData, PackingLayerData>(
  "Packing",
  PackingData(),
  { PackingLayerData() }) {
  init {
    numLayers = 1
  }

  override fun drawOnce(layer: LayerInfo) {
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
    ) = layer.globalValues

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
      pointSet.makeHull().toRPath(closed = true).draw()
    }
  }
}

@Serializable
data class PackingLayerData(
  var PackingField: Int = 1,
) : PropData<PackingLayerData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "L",
      intProp(::PackingField, 1..100)
    )
  )

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
    strength = Point(0, 0)
  ),
  var dotNoise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(0, 0)
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
  override fun BaseSketch.bind() = listOf(
    tab(
      "Packing",
      noiseProp(::centroidNoise),
      noiseProp(::dotNoise),
      row(
        booleanProp(::drawDots),
        booleanProp(::boundDotsToCircle),
      ),
      row(
        intProp(::numDots, 0..100_000),
        intProp(::numCentroids, 1..10_000),
      ),
      intProp(::iterations, 1..32),
      booleanProp(::equalCardinality),
      doubleProp(::circleSize, 50.0..800.0),
      doublePairProp(::circleOffset, NegativeOneToOne * sizeX to NegativeOneToOne * sizeY),
    )
  )

  override fun clone(): PackingData = copy()
  override fun toSerializer() = serializer()
}

fun main() = Packing().run()
