package sketches

import controls.ControlField.Companion.booleanField
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.noiseField
import controls.ControlGroup
import controls.ControlGroupable
import controls.PropFields
import controls.Props
import controls.controls
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import geomerativefork.src.util.flatMapArray
import sketches.Flower.FlowerTab
import sketches.Flower.GlobalTab
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.geomutil.toRShape
import util.times

class Flower : LayeredCanvasSketch<FlowerTab, GlobalTab>("Flower") {
  init {
    numLayers.set(MAX_LAYERS)
  }

  var unionShape: RShape? = null

  override fun drawSetup(drawInfo: DrawInfo) {
    unionShape = null
  }

  override fun drawOnce(layer: LayerInfo) {
    val layerIndex = layer.layerIndex
    val (
      clipToBounds,
      noise,
      numCircles,
      maxRad,
      minRad,
      baseNumInternalCircles,
      distBetweenNoisePerCircle
    ) = layer.globalValues

    val (
      distBetweenInternalCircles,
      numInternalCircles
    ) = layer.tabValues

    if (layerIndex > numCircles) return

    val circleNoise = Noise(
      noise,
      offset = noise.offset + (distBetweenNoisePerCircle * layerIndex))

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

        val movedOriginalPoint = circleNoise.moveRadially(originalPoint, center) { noiseVal ->
          (noiseVal) * circleNoise.strength.magnitude * (if (layerIndex == 0) 0 else 1)
        }

        return@walk it + (movedOriginalPoint - originalPoint)
      }

      val s = warpedCircle
        .toRShape().also { it.addClose() }

      if (unionShape == null) {
        unionShape = RShape(s)
        shape(warpedCircle)
        return@times
      }

      val nonNullUnionShape = unionShape ?: return@times
      var sDiffed: Array<RPath> = arrayOf(s.paths[0])

      nonNullUnionShape.paths.forEach { unionPath ->
        sDiffed = sDiffed.flatMapArray { it.diff(unionPath) }
      }

      if (amountAlongInnerCircle == 0.0) {
        unionShape = nonNullUnionShape.union(s)
      }

      sDiffed.forEach { splitPath ->
        if (clipToBounds)
          shape(splitPath.points.map { Point(it.x, it.y) }, boundRect)
        else
          splitPath.draw()
      }
    }
  }

  data class FlowerTab(
    val distBetweenInternalCircles: Double = 10.0,
    val numInternalCircles: Int = 1,
  )

  data class GlobalTab(
    val clipToBounds: Boolean = false,
    val noise: Noise = Noise(
      seed = 100,
      noiseType = Cubic,
      quality = High,
      scale = 1.0,
      offset = Point.Zero,
      strength = Point(10, 0)
    ),
    val numCircles: Int = 10,
    val maxRad: Double = 300.0,
    val minRad: Double = 30.0,
    val baseNumInternalCircles: Int = 1,
    val distBetweenNoisePerCircle: Double = 150.0,
  )

  override fun initProps(): Props<FlowerTab, GlobalTab> =
    object : Props<FlowerTab, GlobalTab>(maxLayers) {
      override fun globalControls(): PropFields<GlobalTab> =
        object : PropFields<GlobalTab> {
          private val defaults = GlobalTab()
          val clipToBounds = booleanField(defaults::clipToBounds)
          val numCircles = intField(defaults::numCircles, 1..MAX_LAYERS)
          val maxRad = doubleField(defaults::maxRad, 100.0..2000.0)
          val minRad = doubleField(defaults::minRad, 0.0..400.0)
          val baseNumInternalCircles = intField(defaults::baseNumInternalCircles, 1..100)
          val distBetweenNoisePerCircle =
            doubleField(defaults::distBetweenNoisePerCircle, 0.0..150.0)

          private val noiseField = noiseField(defaults::noise)

          override fun toControls(): List<ControlGroupable> = controls(
            clipToBounds,
            ControlGroup(numCircles, baseNumInternalCircles),
            distBetweenNoisePerCircle,
            ControlGroup(minRad, maxRad),
            noiseField
          )

          override fun toValues(): GlobalTab = GlobalTab(
            clipToBounds.get(),
            noiseField.get().clone(),
            numCircles.get(),
            maxRad.get(),
            minRad.get(),
            baseNumInternalCircles.get(),
            distBetweenNoisePerCircle.get(),
          )
        }

      override fun tabControls(tabIndex: Int): PropFields<FlowerTab> =
        object : PropFields<FlowerTab> {
          private val defaults = FlowerTab()
          private val distBetweenInternalCirclesField =
            doubleField(defaults::distBetweenInternalCircles, 1.0..200.0)
          private val numInternalCirclesField = intField(defaults::numInternalCircles, 0..200)

          override fun toControls(): List<ControlGroupable> =
            controls(distBetweenInternalCirclesField, numInternalCirclesField)

          override fun toValues() =
            FlowerTab(distBetweenInternalCirclesField.get(), numInternalCirclesField.get())
        }
    }
}

fun main() = Example().run()
