package sketches

import BaseSketch
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

class Packing : LayeredCanvasSketch<Tab, Global>("Packing", Global(), { Tab() }) {
  init {
    numLayers = 1
  }

  override fun drawOnce(layer: LayerInfo) {
    val (
      noise,
      boundDotsToCircle,
      drawDots,
      numDots,
      numCentroids,
      equalCardinality,
      circleSize,
      circleOffset,
    ) = layer.globalValues

    val c = Circ(center + circleOffset, circleSize)

    val points = numDots.map { randomPoint(c) }.letWith {
      return@letWith if (boundDotsToCircle) filter { c.contains(it) } else this
    }

    val centroids = noise.move(numCentroids.map { randomPoint(boundRect) }) { it * noise.strength }.letWith {
      return@letWith if (boundDotsToCircle) filter { c.contains(it) } else this
    }

    val pointSets = points.kMeans(centroids, equalCardinality)

    pointSets.forEach { pointSet ->
      if (drawDots) pointSet.drawPoints()
      pointSet.makeHull().toRPath().apply { addClose() }.draw()
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
  var noise: Noise = Noise(
    seed = 100,
    noiseType = Cubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0)
  ),
  var boundDotsToCircle: Boolean = true,
  var drawDots: Boolean = true,
  var numDots: Int = 1000,
  var numCentroids: Int = 5,
  var equalCardinality: Boolean = false,
  var circleSize: Double = 300.0,
  var circleOffset: Point = Point.Zero,
) : Bindable {

  override fun bind(s: BaseSketch): List<ControlGroupable> = controls(
    s.booleanProp(this::boundDotsToCircle),
    s.noiseProp(this::noise),
    s.booleanProp(this::drawDots),
    s.intProp(this::numDots, 0..100_000),
    s.intProp(this::numCentroids, 1..10_000),
    s.booleanProp(this::equalCardinality),
    s.doubleProp(this::circleSize, 50.0..800.0),
    s.doublePairProp(this::circleOffset, -s.sizeX.toDouble()..s.sizeX.toDouble() to -s.sizeY.toDouble()..s.sizeY.toDouble()),
  )
}

fun main() = Packing().run()
