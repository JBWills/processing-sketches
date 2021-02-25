package sketches

import BaseSketch
import appletExtensions.withStroke
import controls.ControlGroup
import controls.ControlGroupable
import controls.booleanProp
import controls.controls
import controls.doublePairProp
import controls.doubleProp
import controls.intProp
import controls.noiseProp
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import interfaces.Bindable
import sketches.base.LayeredCanvasSketch
import util.algorithms.kMeans
import util.algorithms.makeHull
import util.geomutil.toRPath
import util.letWith
import util.map
import util.randomPoint
import java.awt.Color

class Packing : LayeredCanvasSketch<Tab, Global>("Packing", Global(), { Tab() }) {
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

data class Tab(
  var PackingField: Int = 1
) : Bindable {
  override fun bind(s: BaseSketch): List<ControlGroupable> =
    controls(s.intProp(this::PackingField, 1..100))
}

data class Global(
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
) : Bindable {

  override fun bind(s: BaseSketch): List<ControlGroupable> = controls(
    s.noiseProp(::centroidNoise),
    s.noiseProp(::dotNoise),
    ControlGroup(
      s.booleanProp(::drawDots),
      s.booleanProp(::boundDotsToCircle),
    ),
    ControlGroup(
      s.intProp(::numDots, 0..100_000),
      s.intProp(::numCentroids, 1..10_000),
    ),
    s.intProp(::iterations, 1..32),
    s.booleanProp(::equalCardinality),
    s.doubleProp(::circleSize, 50.0..800.0),
    s.doublePairProp(::circleOffset, -s.sizeX.toDouble()..s.sizeX.toDouble() to -s.sizeY.toDouble()..s.sizeY.toDouble()),
  )
}

fun main() = Packing().run()
