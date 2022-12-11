package sketches

import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.HarmonicProp
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.layers.LayerSVGConfig
import util.print.Style
import java.awt.Color

/**
 * Inspired by https://www.bit-101.com/blog/2022/11/coding-curves-05-harmonographs/
 */
class HarmonographSketch :
  SimpleCanvasSketch<HarmonographData>("Harmonograph", HarmonographData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (harmonic, harmonic2) = drawInfo.dataValues

    onNextLayer(LayerSVGConfig("pen", style = Style(color = Color.GRAY)))

    harmonic
      .harmonic(boundRect, harmonic2)
      .draw(boundRect)
  }
}

@Serializable
data class HarmonographData(
  var harmonic: HarmonicProp = HarmonicProp(controlsLabel = "PenHarmonic"),
  var harmonic2: HarmonicProp = HarmonicProp(controlsLabel = "PaperHarmonic"),
) : PropData<HarmonographData> {
  override fun bind() = tabs {
    tab("pen harmonic") {
      style = ControlStyle.Red
      panel(::harmonic)
    }
    tab("paper harmonic") {
      style = ControlStyle.Red
      panel(::harmonic2)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = HarmonographSketch().run()
