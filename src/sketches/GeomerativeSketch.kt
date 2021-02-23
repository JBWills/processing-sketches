package sketches

import BaseSketch
import controls.*
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
import interfaces.Bindable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.geomutil.toRShape
import java.awt.Color
import kotlin.math.max

class GeomerativeSketch : LayeredCanvasSketch<GeomTab, GeomValues>("GeomerativeSketch", GeomValues(), { GeomTab() }) {
  init {
    numLayers = 1
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
}

data class GeomTab(var numInternalCircles: Int = 1) : Bindable {
  override fun bind(s: BaseSketch) = controls(
    s.intProp(::numInternalCircles, 1..20)
  )
}

data class GeomValues(
  var numCircles: Int = 4,
  var maxRad: Double = 400.0,
  var minRad: Double = 10.0,
  var distBetweenNoisePerCircle: Double = 0.0,
  var numExtraStrokesToDraw: Int = 1,
  var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0)
  )
) : Bindable {
  override fun bind(s: BaseSketch) = controls(
    s.intProp(::numCircles, 1..40),
    ControlGroup(
      s.doubleProp(::maxRad, 100.0..2000.0),
      s.doubleProp(::minRad, 0.0..400.0),
    ),
    s.doubleProp(::distBetweenNoisePerCircle, 0.0..150.0),
    s.intProp(::numExtraStrokesToDraw, 0..100),
    s.noiseProp(::noise),
  )
}

fun main() = GeomerativeSketch().run()
