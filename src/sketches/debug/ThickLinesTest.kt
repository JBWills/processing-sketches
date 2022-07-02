package sketches.debug

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.SineWaveProp
import coordinate.ThickPolyLine.Companion.toThickLine
import coordinate.Thickness
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.layers.LayerSVGConfig
import util.polylines.iterators.walkWithCursor

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class ThickLinesTest : SimpleCanvasSketch<ThickLinesData>("ThickLines", ThickLinesData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (sinWave1, sinWave2, sinWave3, drawOriginal, maxPxDistBetweenLines, sampleFreq) = drawInfo.dataValues

    boundRect.centerLineHorizontal.expand(-10)
      .toPolyLine()
      .walkWithCursor(1.0) { it.point.addY(sinWave1.f(it.percent)) }
      .toThickLine(
        numKeyFrames = (1.0 / sampleFreq).toInt(),
        maxPxDistBetweenLines = maxPxDistBetweenLines,
        drawOriginal = drawOriginal,
      ) {
        Thickness(
          amount = sinWave2.f(it.percent),
          centerAmount = sinWave3.f(it.percent),
        )
      }
      .toLines()
      .draw(boundRect)
  }
}

@Serializable
data class ThickLinesData(
  var mainWave: SineWaveProp = SineWaveProp(amplitude = 200),
  var thicknessWave: SineWaveProp = SineWaveProp(amplitude = 50, freq = 4),
  var offsetWave: SineWaveProp = SineWaveProp(amplitude = 0, freq = 4, ampMax = 1.0),
  var drawOriginal: Boolean = true,
  var maxPxDistBetweenLines: Double = 3.0,
  var sampleFreq: Double = 0.05,
) : PropData<ThickLinesData> {
  override fun bind() = tabs {
    tab("Global") {
      panel(::mainWave)
      panel(::thicknessWave)
      panel(::offsetWave)
      slider(::sampleFreq, 0.001..0.5)
      slider(::maxPxDistBetweenLines, 1.0..20.0)
      toggle(::drawOriginal)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ThickLinesTest().run()
