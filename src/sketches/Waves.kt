package sketches

import BaseSketch
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.Point
import coordinate.Segment
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import geomerativefork.src.util.bound
import sketches.base.EmptyConfig
import sketches.base.LayeredCanvasSketch
import util.RangeWithCurrent.Companion.at
import util.atAmountAlong
import util.geomutil.toRPath
import util.percentAlong
import util.randomLightColor
import util.withRed
import java.awt.Color

class Waves : LayeredCanvasSketch("Waves") {
  val numCircles = intField("numCircles", 1..MAX_LAYERS at MAX_LAYERS)
  val maxHeight = doubleField("maxHeight", 100.0..2000.0 at boundRect.bottom)
  val minHeight = doubleField("minHeight", 0.0..400.0 at boundRect.top)
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
      ControlGroup(minHeight, maxHeight),
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

    val waveNum = layer - 1

    val tab = tabs[layer - 1]

    val waveNoise = Noise(
      noise,
      offset = noise.offset + (distBetweenNoisePerCircle.get() * waveNum))

    val baseColor = randomLightColor()

    fun waveAmountAlong(n: Int) = (n.toDouble() + 1) / numCircles.get()

    val baseHeight = (maxHeight.get()..minHeight.get())
      .atAmountAlong(waveAmountAlong(waveNum))
    val lastHeight = (maxHeight.get()..minHeight.get())
      .atAmountAlong(waveAmountAlong(waveNum - 1))

    val maxLineHeight = lastHeight + 2 * waveNoise.strength.y

    var height = baseHeight

    var nextUnionShape: RShape? = null

    val baseWarpedPoints = Segment(Point(0, height), Point(sizeX, height))
      .warped(waveNoise)

    while (height < maxLineHeight) {
      val percentAlong = (maxLineHeight..baseHeight).percentAlong(height)
      val warpedPath = baseWarpedPoints.map {
        it + Point(0, height - baseHeight)
      }
        .toRPath()

      if (height == baseHeight) {
        val waveShape = RShape(warpedPath.apply {
          addLineTo(sizeX.toFloat(), sizeY.toFloat())
          addLineTo(0f, sizeY.toFloat())
          addClose()
        })

        nextUnionShape = if (unionShape == null) waveShape else unionShape?.union(waveShape)
      }

      val warpedPaths: Array<RPath> =
        unionShape?.let { warpedPath.diff(it.paths[0]) } ?: arrayOf(warpedPath)


      if (height == baseHeight)
        stroke(Color.white.rgb)
      else
        stroke(baseColor.withRed(percentAlong.toFloat().bound()).rgb)

      warpedPaths.forEach { shape(it, boundRect) }

      height += tab.distBetweenLines.get()
    }

    unionShape = nextUnionShape
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(tabs[index].offset, tabs[index].distBetweenLines)

  inner class WavesTab {
    val distBetweenLines = doubleField("distBetweenLines", 1.0..200.0 at 10)
    val offset = doubleField("offset", -200.0..200.0 at 0)
  }
}

fun main() = BaseSketch.run(Waves())
