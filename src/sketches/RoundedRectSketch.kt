package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.FineSliderArgs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.fineSlider
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Deg
import coordinate.Point
import coordinate.times
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.base.doIf
import util.layers.LayerSVGConfig
import util.numbers.pow
import util.polylines.rotate
import util.print.Style
import util.shapeExt.toRoundedRect
import java.awt.Color

/**
 * Rounded window into nothing
 */
class RoundedRectSketch : SimpleCanvasSketch<RoundedRectData>("RoundedRect", RoundedRectData()) {

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    onNextLayer(LayerSVGConfig("ghosts", style = Style(color = Color.GRAY)))
    val (baseData, noiseData) = drawInfo.dataValues
    val (rectOrigin, rectSize, roundingAmount, numRects, falloff, radiusFalloff) = baseData

    val rect = centeredRect(
      center = boundRect.pointAt(rectOrigin + Point.Half),
      size = (boundRect.height / 3) * rectSize,
    )

    fun drawRect(rectIndex: Int) {
      val offsetNoise = noiseData.noise.with(offsetZ = rectIndex * noiseData.noiseEvolution)
      val scaleFactor = (1 - falloff).pow(rectIndex)
      val radiusScaleFactor = (1 - radiusFalloff).pow(rectIndex)
      rect.scaled(Point(scaleFactor, scaleFactor), rect.center)
        .toRoundedRect(roundingAmount * radiusScaleFactor)
        .polyLine
        .doIf(rectIndex != 0) {
          it
            .rotate(Deg(noiseData.rotation.value * rectIndex), rect.center)
            .warped(offsetNoise) { point ->
              point * (1 - (1 + noiseData.noiseStrengthFalloff).pow(
                rectIndex,
              ))
            }
        }
        .draw(boundRect)
    }

    (1..numRects).forEach(::drawRect)

    onNextLayer(LayerSVGConfig(nextLayerName = "main", style = Style(color = Color.WHITE)))
    drawRect(0)
  }
}

@Serializable
data class RoundedRectNoiseData(
  var rotation: Deg = Deg(0),
  var noiseEvolution: Double = 2.0,
  var noiseStrengthFalloff: Double = 0.1,
  var noise: Noise = Noise.DEFAULT,
)

@Serializable
data class RoundedRectBaseData(
  var rectOrigin: Point = Point.Zero,
  var rectSize: Point = Point.One,
  var roundingAmount: Double = 0.0,
  var numRects: Int = 20,
  var falloff: Double = 0.1,
  var radiusFalloff: Double = 0.1,
)

@Serializable
data class RoundedRectData(
  var baseData: RoundedRectBaseData = RoundedRectBaseData(),
  var noiseData: RoundedRectNoiseData = RoundedRectNoiseData(),
) : PropData<RoundedRectData> {
  override fun bind() = tabs {
    tab("Global") {
      slider2D(baseData::rectOrigin, Slider2DArgs(-1.0..8.0))
      sliderPair(baseData::rectSize, SliderPairArgs(0.0..5.0, withLockToggle = true))

      slider(baseData::roundingAmount, 0.0..1.0)
      slider(baseData::numRects, 1..100)
      row {
        col {
          style = ControlStyle.Gray
          fineSlider(baseData::falloff, FineSliderArgs(0.0..1.0, 0.0..0.01))
        }
        col {
          style = ControlStyle.Green
          fineSlider(baseData::radiusFalloff, FineSliderArgs(0.0..1.0, 0.0..0.01))
        }
      }
    }

    tab("noise") {
      slider(noiseData::rotation, -90.0..90.0)
      slider(noiseData::noiseEvolution, 0.0..100.0)
      slider(noiseData::noiseStrengthFalloff, 0.0..1.0)
      noisePanel(noiseData::noise)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = RoundedRectSketch().run()
