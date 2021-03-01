package sketches

import BaseSketch
import appletExtensions.withStroke
import controls.ControlGroup.Companion.group
import controls.ControlTab.Companion.tab
import controls.booleanProp
import controls.doublePairProp
import controls.doubleProp
import controls.intProp
import controls.noiseProp
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import interfaces.Copyable
import interfaces.TabBindable
import sketches.base.LayeredCanvasSketch
import util.NegativeOneToOne
import util.algorithms.kMeans
import util.algorithms.makeHull
import util.geomutil.toRPath
import util.letWith
import util.map
import util.randomPoint
import util.times
import java.awt.Color

class Packing : LayeredCanvasSketch<PackingLayerData, PackingData>("Packing", PackingData(), { PackingLayerData() }) {
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

data class PackingLayerData(
  var PackingField: Int = 1
) : TabBindable, Copyable<PackingLayerData> {
  override fun BaseSketch.bindTab() = tab(
    "L",
    intProp(::PackingField, 1..100)
  )

  override fun clone(): PackingLayerData = copy()
}

data class PackingData(
  var centroidNoise: Noise = Noise(
    seed = 100,
    noiseType = Cubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(0, 0)
  ),
  var dotNoise: Noise = Noise(
    seed = 100,
    noiseType = Cubic,
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
) : TabBindable, Copyable<PackingData> {
  override fun BaseSketch.bindTab() = tab(
    "Packing",
    noiseProp(::centroidNoise),
    noiseProp(::dotNoise),
    group(
      booleanProp(::drawDots),
      booleanProp(::boundDotsToCircle),
    ),
    group(
      intProp(::numDots, 0..100_000),
      intProp(::numCentroids, 1..10_000),
    ),
    intProp(::iterations, 1..32),
    booleanProp(::equalCardinality),
    doubleProp(::circleSize, 50.0..800.0),
    doublePairProp(::circleOffset, NegativeOneToOne * sizeX to NegativeOneToOne * sizeY),
  )

  override fun clone(): PackingData = copy()
}

fun main() = Packing().run()
