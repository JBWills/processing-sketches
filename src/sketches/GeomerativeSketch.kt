package sketches

import BaseSketch
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedRadially
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import geomerativefork.src.util.flatMapArray
import sketches.base.LayeredCanvasSketch
import util.RangeWithCurrent.Companion.at
import util.atAmountAlong
import util.geomutil.toRShape
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class GeomerativeSketch : LayeredCanvasSketch("GeomerativeSketch") {

  val numCircles = intField("numCircles", 1..40 at 4)
  val maxRad = doubleField("maxRad", 100.0..2000.0 at 400.0)
  val minRad = doubleField("minRad", 0.0..400.0 at 10)
  val distBetweenNoisePerCircle = doubleField("distBetweenNoisePerCircle", 0.0..150.0 at 0)
  val numExtraStrokesToDraw = intField("numExtraStrokesToDraw", 0..100 at 1)

  val tabs: List<TabControls> = (1..MAX_LAYERS).map { TabControls() }

  private var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0)
  )

  override fun getGlobalControls(): Array<ControlGroupable> =
    arrayOf(
      numCircles,
      distBetweenNoisePerCircle,
      numExtraStrokesToDraw,
      ControlGroup(minRad, maxRad),
      *noiseControls(::noise)
    )

  init {
    numLayers.set(1)
  }

  var unionShape: RShape? = null
  override fun drawOnce(layer: Int) {
    (0 until numCircles.get()).forEach { idx ->
      val circleNoise = Noise(
        noise,
        offset = noise.offset + (distBetweenNoisePerCircle.get() * idx))

      val firstIndex = if (idx == 0) 0 else -numExtraStrokesToDraw.get()

      (firstIndex until tabs[0].numInternalCircles.get()).map { innerCircleIndex ->
        val amountAlongInnerCircle =
          (innerCircleIndex.toDouble() + 1) / tabs[0].numInternalCircles.get()

        stroke(Color((min(1.0, max(0.0, amountAlongInnerCircle)) * 255.0).toInt(), 50, 50).rgb)

        val radius =
          max(0.0, (minRad.get()..maxRad.get()).atAmountAlong(
            (idx + amountAlongInnerCircle) / (numCircles.get() + 1)))

        if (radius < minRad.get()) return@map

        val warpedCircle = Circ(center, radius)
          .warpedRadially(circleNoise) { noiseVal ->
            (noiseVal) * noise.strength.magnitude * max(0.5,
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

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(tabs[index].numInternalCircles)

  inner class TabControls {
    val numInternalCircles =
      intField("numInternalCircles", 1..20 at 1)
  }
}

fun main() = BaseSketch.run(GeomerativeSketch())
