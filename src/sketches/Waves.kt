package sketches

import BaseSketch
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import geomerativefork.src.util.bound
import geomerativefork.src.util.flatMapArray
import sketches.base.EmptyConfig
import sketches.base.LayeredCanvasSketch
import util.RangeWithCurrent.Companion.at
import util.atAmountAlong
import util.geomutil.toRShape
import util.randomLightColor
import util.times
import util.withRed
import java.awt.Color

class Waves : LayeredCanvasSketch("Waves") {
  val numCircles = intField("numCircles", 1..MAX_LAYERS at MAX_LAYERS)
  val maxRad = doubleField("maxRad", 100.0..2000.0 at 300.0)
  val minRad = doubleField("minRad", 0.0..400.0 at 30)
  val baseNumInternalCircles = intField("baseNumInternalCircles", 1..100 at 1)
  val distBetweenNoisePerCircle = doubleField("distBetweenNoisePerCircle", 0.0..150.0 at 150)

  val tabs: List<WavesTab> = (1..MAX_LAYERS).map { WavesTab() }

  private var noise: Noise = Noise(
    seed = 100,
    noiseType = Cubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0)
  )

  override fun getGlobalControls(): Array<ControlGroupable> =
    arrayOf(
      ControlGroup(numCircles, baseNumInternalCircles),
      distBetweenNoisePerCircle,
      ControlGroup(minRad, maxRad),
      *noiseControls(::noise)
    )

  init {
    numLayers.set(MAX_LAYERS)
  }

  var unionShape: RShape? = null

  override fun drawSetup(sketchConfig: EmptyConfig) {
    unionShape = null
  }

  override fun drawOnce(layer: Int) {
    if (layer == 0 || layer > numCircles.get()) return

    val circleNum = layer - 1

    val tab = tabs[layer - 1]

    val circleNoise = Noise(
      noise,
      offset = noise.offset + (distBetweenNoisePerCircle.get() * circleNum))

    val baseColor = randomLightColor()

    val baseRadius = (minRad.get()..maxRad.get())
      .atAmountAlong((circleNum.toDouble() + 1) / numCircles.get())

    val baseCircle = Circ(center, baseRadius)

    val totalInternalCircles = baseNumInternalCircles.get() + tab.numInternalCircles.get()

    totalInternalCircles.times { indexInverted ->
      val innerCircleIndex = (totalInternalCircles - 1) - indexInverted
      val amountAlongInnerCircle =
        innerCircleIndex.toDouble() / totalInternalCircles

      stroke(baseColor.withRed(amountAlongInnerCircle.toFloat().bound()).rgb)

      val radius = baseRadius - (innerCircleIndex * tab.distBetweenInternalCircles.get())

      if (radius < minRad.get()) return@times

      val c = Circ(center, radius)
      val warpedCircle = c.walk(circleNoise.quality.step) {
        val originalPoint = baseCircle.pointAtAngle(c.angleAtPoint(it))

        val movedOriginalPoint = circleNoise.moveRadially(originalPoint, center) { noiseVal ->
          (noiseVal) * circleNoise.strength.magnitude * (if (circleNum == 0) 0 else 1)
        }

        return@walk it + (movedOriginalPoint - originalPoint)
      }

      val s = warpedCircle
        .toRShape().also { it.addClose() }

      if (unionShape == null) {
        unionShape = RShape(s)
        stroke(Color.white.rgb)
        shape(warpedCircle)
        return@times
      }

      unionShape?.let { nonNullUnionShape ->
        var sDiffed: Array<RPath> = arrayOf(s.paths[0])

        nonNullUnionShape.paths.forEach { unionPath ->
          sDiffed = sDiffed.flatMapArray { it.diff(unionPath) }
        }

        if (amountAlongInnerCircle == 0.0) {
          unionShape = nonNullUnionShape.union(s)
        }

        sDiffed.forEach { splitPath ->
          splitPath.draw()
        }
      }
    }
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(tabs[index].numInternalCircles, tabs[index].distBetweenInternalCircles)

  inner class WavesTab {
    val numInternalCircles =
      intField("numInternalCircles", 0..200 at 0)
    val distBetweenInternalCircles = doubleField("distBetweenInternalCircles", 1.0..100.0 at 10)
  }
}

fun main() = BaseSketch.run(Waves())
