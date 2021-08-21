package sketches

import FastNoiseLite.NoiseType.ValueCubic
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.noisePanel
import controls.panels.panelext.slider
import controls.props.PropData
import coordinate.Point
import coordinate.Segment
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.UNION
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.polylines.PolyLine
import util.polylines.clipping.clip

class Waves : LayeredCanvasSketch<WaveGlobal, WaveTab>("Waves", WaveGlobal(), { WaveTab() }) {
  init {
    numLayers = MAX_LAYERS
  }

  var unionShape: List<PolyLine>? = null

  override fun drawSetup(layerInfo: DrawInfo) {
    unionShape = null
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val layer = layerInfo.layerIndex
    val values = layerInfo.globalValues
    val tabValues = layerInfo.tabValues

    if (layer > values.numCircles - 1) return

    val waveNoise = Noise(
      values.noise,
      offset = values.noise.offset + (values.distBetweenNoisePerCircle * layer),
    )

    fun waveAmountAlong(n: Int) = (n.toDouble() + 1) / values.numCircles

    val baseHeight = (values.maxHeight..values.minHeight)
      .atAmountAlong(waveAmountAlong(layer))
    val lastHeight = (values.maxHeight..values.minHeight)
      .atAmountAlong(waveAmountAlong(layer - 1))

    val maxLineHeight = lastHeight + 2 * waveNoise.strength.y

    var height = baseHeight

    var nextUnionShape: List<PolyLine>? = null

    val baseWarpedPoints = Segment(Point(0, height), Point(size.x, height))
      .warped(waveNoise)

    while (height < maxLineHeight) {
      val warpedPath = baseWarpedPoints.map {
        it + Point(0, height - baseHeight)
      }

      if (height == baseHeight) {
        val waveShape = (warpedPath + size) + size.withX(0)

        nextUnionShape =
          if (unionShape == null) listOf(waveShape) else unionShape?.clip(waveShape, UNION)
      }

      val warpedPaths: List<PolyLine> =
        unionShape?.let { warpedPath.clip(it, ClipType.DIFFERENCE) } ?: listOf(warpedPath)

      warpedPaths.draw(boundRect)

      height += tabValues.distBetweenLines
    }

    unionShape = nextUnionShape
  }
}

@Serializable
data class WaveTab(
  var distBetweenLines: Double = 10.0,
  var offset: Double = 0.0,
) : PropData<WaveTab> {
  override fun bind() = layerTab {
    slider(::distBetweenLines, 1.0..200.0)
    slider(::offset, -200.0..200.0)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class WaveGlobal(
  var noise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0),
  ),
  var numCircles: Int = 5,
  var maxHeight: Double = 500.0,
  var minHeight: Double = 0.0,
  var baseNumInternalCircles: Int = 1,
  var distBetweenNoisePerCircle: Double = 150.0,
) : PropData<WaveGlobal> {
  override fun bind() = singleTab("Waves") {
    slider(::numCircles, 1..LayeredCanvasSketch.MAX_LAYERS)
    row {
      slider(::maxHeight, 100.0..2000.0)
      slider(::minHeight, -400.0..400.0)
    }
    slider(::baseNumInternalCircles, 1..100)
    slider(::distBetweenNoisePerCircle, 0.0..150.0)
    noisePanel(::noise)
  }

  override fun clone(): WaveGlobal = copy()
  override fun toSerializer() = serializer()
}

fun main() = Waves().run()
