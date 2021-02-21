package sketches

import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.noiseField
import controls.ControlGroup
import controls.ControlGroupable
import controls.PropFields
import controls.Props
import controls.controls
import coordinate.Point
import coordinate.Segment
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import geomerativefork.src.RPath
import geomerativefork.src.RShape
import sketches.Waves.GlobalTab
import sketches.Waves.WaveTab
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.geomutil.toRPath

class Waves : LayeredCanvasSketch<WaveTab, GlobalTab>("Waves") {
  init {
    numLayers.set(MAX_LAYERS)
  }

  var unionShape: RShape? = null

  override fun drawSetup(values: DrawInfo) {
    unionShape = null
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val layer = layerInfo.layerIndex
    val values = layerInfo.globalValues
    val tabValues = layerInfo.tabValues

    if (layer > values.numCircles - 1) return

    val waveNoise = Noise(
      values.noise,
      offset = values.noise.offset + (values.distBetweenNoisePerCircle * layer))

    fun waveAmountAlong(n: Int) = (n.toDouble() + 1) / values.numCircles

    val baseHeight = (values.maxHeight..values.minHeight)
      .atAmountAlong(waveAmountAlong(layer))
    val lastHeight = (values.maxHeight..values.minHeight)
      .atAmountAlong(waveAmountAlong(layer - 1))

    val maxLineHeight = lastHeight + 2 * waveNoise.strength.y

    var height = baseHeight

    var nextUnionShape: RShape? = null

    val baseWarpedPoints = Segment(Point(0, height), Point(sizeX, height))
      .warped(waveNoise)

    while (height < maxLineHeight) {
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

      warpedPaths.forEach { shape(it, boundRect) }

      height += tabValues.distBetweenLines
    }

    unionShape = nextUnionShape
  }

  inner class WaveTab(
    val distBetweenLines: Double = 10.0,
    val offset: Double = 0.0,
  )

  inner class GlobalTab(
    val noise: Noise = Noise(
      seed = 100,
      noiseType = Cubic,
      quality = High,
      scale = 1.0,
      offset = Point.Zero,
      strength = Point(10, 0)
    ),
    val numCircles: Int = maxLayers,
    val maxHeight: Double = boundRect.bottom,
    val minHeight: Double = boundRect.top,
    val baseNumInternalCircles: Int = 1,
    val distBetweenNoisePerCircle: Double = 150.0,
  )

  override fun initProps(): Props<WaveTab, GlobalTab> =
    object : Props<WaveTab, GlobalTab>(maxLayers) {
      override fun globalControls(): PropFields<GlobalTab> =
        object : PropFields<GlobalTab>() {
          private val defaults = GlobalTab()
          private val numCirclesField = intField(defaults::numCircles, 1..MAX_LAYERS)
          private val maxHeightField = doubleField(defaults::maxHeight, 100.0..2000.0)
          private val minHeightField = doubleField(defaults::minHeight, 0.0..400.0)
          private val baseNumInternalCirclesField =
            intField(defaults::baseNumInternalCircles, 1..100)
          private val distBetweenNoisePerCircleField =
            doubleField(defaults::distBetweenNoisePerCircle, 0.0..150.0)

          private val noiseField = noiseField(defaults::noise)

          override fun toControls(): List<ControlGroupable> = controls(
            ControlGroup(numCirclesField, baseNumInternalCirclesField),
            distBetweenNoisePerCircleField,
            ControlGroup(minHeightField, maxHeightField),
            noiseField
          )

          override fun toValues(): GlobalTab = GlobalTab(
            noiseField.get().clone(),
            numCirclesField.get(),
            maxHeightField.get(),
            minHeightField.get(),
            baseNumInternalCirclesField.get(),
            distBetweenNoisePerCircleField.get(),
          )
        }

      override fun tabControls(tabIndex: Int): PropFields<WaveTab> =
        object : PropFields<WaveTab>() {
          private val defaults = WaveTab()
          private val distBetweenLinesField = doubleField(defaults::distBetweenLines, 1.0..200.0)
          private val offsetField = doubleField(defaults::offset, -200.0..200.0)

          override fun toControls(): List<ControlGroupable> =
            controls(distBetweenLinesField, offsetField)

          override fun toValues() = WaveTab(distBetweenLinesField.get(), offsetField.get())
        }
    }
}

fun main() = Waves().run()
