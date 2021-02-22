package sketches

import controls.ControlField.Companion.booleanField
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.noiseField
import controls.PropFields
import controls.Props
import controls.controls
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import sketches.Packing.Global
import sketches.Packing.Tab
import sketches.base.LayeredCanvasSketch
import util.algorithms.kMeans
import util.algorithms.makeHull
import util.geomutil.toRPath
import util.letWith
import util.map
import util.randomPoint

class Packing : LayeredCanvasSketch<Tab, Global>("Packing") {
  init {
    numLayers.set(1)
  }

  override fun drawOnce(layer: LayerInfo) {
    println(layer.layerIndex)
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

  data class Tab(
    val PackingField: Int = 1
  )

  data class Global(
    val noise: Noise = Noise(
      seed = 100,
      noiseType = Cubic,
      quality = High,
      scale = 1.0,
      offset = Point.Zero,
      strength = Point(10, 0)
    ),
    val boundDotsToCircle: Boolean = true,
    val drawDots: Boolean = true,
    val numDots: Int = 1000,
    val numCentroids: Int = 5,
    val equalCardinality: Boolean = false,
    val circleSize: Double = 300.0,
    val circleOffset: Point = Point.Zero,
  )

  override fun initProps(): Props<Tab, Global> =
    object : Props<Tab, Global>(maxLayers) {
      override fun globalControls(): PropFields<Global> =
        object : PropFields<Global>() {
          private val defaults = Global()
          val boundDotsToCircleField = booleanField(defaults::boundDotsToCircle)
          val noiseField = noiseField(defaults::noise)
          val drawDotsField = booleanField(defaults::drawDots)
          val numDotsField = intField(defaults::numDots, 0..100_000)
          val numCentroidsField = intField(defaults::numCentroids, 1..10_000)
          val equalCardinalityField = booleanField(defaults::equalCardinality)
          val circleSizeField = doubleField(defaults::circleSize, 50.0..800.0)
          val circleOffsetField = doublePairField(defaults::circleOffset, -sizeX.toDouble()..sizeX.toDouble() to -sizeY.toDouble()..sizeY.toDouble())

          override fun toControls() = controls(
            noiseField,
            boundDotsToCircleField,
            drawDotsField,
            numDotsField,
            numCentroidsField,
            equalCardinalityField,
            circleSizeField,
            circleOffsetField,
          )

          override fun toValues() = Global(
            noise = noiseField.get(),
            boundDotsToCircle = boundDotsToCircleField.get(),
            drawDots = drawDotsField.get(),
            numDots = numDotsField.get(),
            numCentroids = numCentroidsField.get(),
            equalCardinality = equalCardinalityField.get(),
            circleSize = circleSizeField.get(),
            circleOffset = circleOffsetField.get(),
          )
        }

      override fun tabControls(tabIndex: Int): PropFields<Tab> =
        object : PropFields<Tab>() {
          private val defaults = Tab()
          val PackingField = intField(defaults::PackingField, 0..100)

          override fun toControls() = controls(PackingField)

          override fun toValues() = Tab(PackingField.get())
        }
    }
}

fun main() = Packing().run()
