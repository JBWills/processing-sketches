package sketches

import controls.*
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.noiseField
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedRadially
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import geomerativefork.src.util.bound
import geomerativefork.src.util.flatMapArray
import sketches.GeomerativeSketch.GlobalFields
import sketches.GeomerativeSketch.TabFields
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.geomutil.toRShape
import java.awt.Color
import kotlin.math.max

class GeomerativeSketch : LayeredCanvasSketch<TabFields, GlobalFields>("GeomerativeSketch") {
  init {
    numLayers.set(1)
  }

  var unionShape: RShape? = null
  override fun drawOnce(layerInfo: LayerInfo) {
    val values = layerInfo.globalValues
    val tabValues = layerInfo.tabValues
    (0 until values.numCircles).forEach { idx ->
      val circleNoise = Noise(
        values.noise,
        offset = values.noise.offset + (values.distBetweenNoisePerCircle * idx))

      val firstIndex = if (idx == 0) 0 else -values.numExtraStrokesToDraw

      (firstIndex until tabValues.numInternalCircles).map { innerCircleIndex ->
        val amountAlongInnerCircle =
          (innerCircleIndex.toDouble() + 1) / tabValues.numInternalCircles

        stroke(Color((amountAlongInnerCircle.bound(0.0, 1.0) * 255.0).toInt(), 50, 50).rgb)

        val radius =
          max(0.0, (values.minRad..values.maxRad).atAmountAlong(
            (idx + amountAlongInnerCircle) / (values.numCircles + 1)))

        if (radius < values.minRad) return@map

        val warpedCircle = Circ(center, radius)
          .warpedRadially(circleNoise) { noiseVal ->
            (noiseVal) * values.noise.strength.magnitude * max(0.5,
              amountAlongInnerCircle) * (idx.toDouble())
          }

        val s = warpedCircle
          .toRShape().also { it.addClose() }

        if (unionShape == null) {
          unionShape = RShape(s)
          stroke(Color.white.rgb)
          shape(warpedCircle)
        } else {
          unionShape?.let { nonNullUnionShape ->
            var sDiffed: Array<RPath> = arrayOf(s.paths[0])

            nonNullUnionShape.paths.forEach { unionPath ->
              sDiffed = sDiffed.flatMapArray { it.diff(unionPath) }
            }

            if (amountAlongInnerCircle == 1.0)
              unionShape = nonNullUnionShape.union(s)

            sDiffed.forEach { splitPath ->
              splitPath.draw()
            }
          }
        }
      }
    }

    unionShape = null
  }

  inner class TabFields(val numInternalCircles: Int = 1)
  inner class GlobalFields(
    val numCircles: Int = 4,
    val maxRad: Double = 400.0,
    val minRad: Double = 10.0,
    val distBetweenNoisePerCircle: Double = 0.0,
    val numExtraStrokesToDraw: Int = 1,
    val noise: Noise = Noise(
      seed = 100,
      noiseType = Perlin,
      quality = High,
      scale = 0.15,
      offset = Point.Zero,
      strength = Point(0, 0)
    )
  )

  override fun initProps(): Props<TabFields, GlobalFields> = object : Props<TabFields, GlobalFields>(maxLayers) {
    override fun globalControls(): PropFields<GlobalFields> = object : PropFields<GlobalFields>() {
      val defaults = GlobalFields()
      val numCircles = intField(defaults::numCircles, 1..40)
      val maxRad = doubleField(defaults::maxRad, 100.0..2000.0)
      val minRad = doubleField(defaults::minRad, 0.0..400.0)
      val distBetweenNoisePerCircle = doubleField(defaults::distBetweenNoisePerCircle, 0.0..150.0)
      val numExtraStrokesToDraw = intField(defaults::numExtraStrokesToDraw, 0..100)
      val noise = noiseField(defaults::noise)
      override fun toControls(): List<ControlGroupable> = controls(
        numCircles,
        distBetweenNoisePerCircle,
        numExtraStrokesToDraw,
        ControlGroup(minRad, maxRad),
        noise
      )

      override fun toValues(): GlobalFields = GlobalFields(
        numCircles = numCircles.get(),
        maxRad = maxRad.get(),
        minRad = minRad.get(),
        distBetweenNoisePerCircle = distBetweenNoisePerCircle.get(),
        numExtraStrokesToDraw = numExtraStrokesToDraw.get()
      )
    }

    override fun tabControls(tabIndex: Int): PropFields<TabFields> = object : PropFields<TabFields>() {
      val defaults = TabFields()
      val numInternalCircles = intField(defaults::numInternalCircles, 1..20)
      override fun toControls(): List<ControlGroupable> = controls(numInternalCircles)
      override fun toValues(): TabFields = TabFields(numInternalCircles = numInternalCircles.get())
    }
  }
}

fun main() = GeomerativeSketch().run()
