package sketches.debug

import appletExtensions.withStyle
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.layers.LayerSVGConfig
import util.numbers.floorInt
import util.print.Style
import java.awt.Color

// Max height of the histogram
const val MaxHeightPx = 200

class NoiseTest : SimpleCanvasSketch<NoiseTestData>("NoiseTest", NoiseTestData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (noise, z) = drawInfo.dataValues

    val startEnd =
      boundRect.centerLineHorizontal.expand(-boundRect.centerLineHorizontal.length / 2).translated(
        Point(0, -50),
      )
    startEnd.draw()

    val buckets: MutableMap<Int, Int> = mutableMapOf()
    var maxInSingleBucket = 0

    (0..10_000).map {
      val value = noise.with(seed = it).get(0, 0, z)
      val roundedValue = (value * 10).floorInt()
      val newCount = buckets.getOrDefault(roundedValue, 0) + 1
      if (newCount > maxInSingleBucket) {
        maxInSingleBucket = newCount
      }
      buckets.put(roundedValue, newCount)
    }

    if (maxInSingleBucket == 0) return

    (-20..20).map { x ->
      val startPoint = startEnd.getPointAtPercent(0.5 + (0.5 * (x / 10.0)))
      val height = (buckets.getOrDefault(x, 0) / maxInSingleBucket.toDouble()) * MaxHeightPx

      if (height != 0.0) {
        withStyle(Style(color = Color.PINK)) {
          Segment(startPoint, startPoint.addY(-height)).draw()
        }
      }


    }
  }
}

@Serializable
data class NoiseTestData(
  var noise: Noise = Noise.DEFAULT,
  var z: Double = 0.0,
) : PropData<NoiseTestData> {
  override fun bind() = tabs {
    tab("Global") {
      noisePanel(::noise)
      slider(::z, 0..1000)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = NoiseTest().run()
