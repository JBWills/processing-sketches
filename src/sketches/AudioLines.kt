package sketches

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.audioSelect
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Point
import data.AmplitudeLine2D
import data.Audio
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.audio.DefaultSampleSize
import util.interpolation.Interpolator1DType
import util.interpolation.Interpolator1DType.CubicSpline1DType
import util.layers.LayerSVGConfig
import util.numbers.map
import java.awt.Color

private val Colors: List<Color> = listOf(
  Color.RED,
  Color.PINK,
  Color.WHITE,
  Color.LIGHT_GRAY,
  Color.BLUE,
  Color.MAGENTA,
)

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class AudioLines : SimpleCanvasSketch<AudioLinesData>("AudioLines", AudioLinesData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (audio, sampleSize, interpolationStep, linesToShow, drawSize, drawCenter, amplitudeScale, numLayers, interpolationType, noise, drawBaseLines, percentThroughSong1) = drawInfo.dataValues

    val features = audio.getFeatures(sampleSize) ?: return

    val pressureSamples = features.pressures.size
    val samplesPerLine = pressureSamples / linesToShow
    val linesBound = centeredRect(boundRect.pointAt(drawCenter), boundRect.size * drawSize)

//    val amplitudeLines = linesToShow.map { lineIndex ->
//      val sampleIndex = (lineIndex * samplesPerLine)
//      val pressures = features
//        .pressuresFrom(sampleIndex..(sampleIndex + samplesPerLine))
//        .mapDoubleArray { it * amplitudeScale }
//      AmplitudeLine2D(pressures, interpolationType.create(), scaleFactor = amplitudeScale)
//    }

    val amplitudeLine2D =
      AmplitudeLine2D(features.pitches, scaleFactor = amplitudeScale)

    // todo add interpolated spectrogram processing here
    numLayers.map { layerIndex ->
      newLayerStyled(stroke = Colors[layerIndex % Colors.size], onNextLayer = onNextLayer) {
        val line = boundRect.topSegment.translated(Point(0, boundRect.height / 2)).expand(-30)
        amplitudeLine2D
          .interpolateAlong(line.toPolyLine(), percentThroughSong1, interpolationStep)
          .draw(boundRect)

//        linesToShow.map { lineIndex ->
//
//          val baseBounds = BoundRect(
//            linesBound.topLeft + Point(0, (lineIndex.toDouble() / linesToShow) * linesBound.height),
//            width = linesBound.width,
//            height = 0,
//          )
//
//          val baseLine = baseBounds.topSegment.warped(noise.with(seed = noise.seed + layerIndex))
//
//          if (drawBaseLines) {
//            withStroke(Color.YELLOW) { baseLine.draw(boundRect) }
//          }
//
//          val percentThroughSong =
//            if (linesToShow == 1) 0.5 else lineIndex.toDouble() / (linesToShow - 1)
//
//          amplitudeLine2D
//            .interpolateAlong(baseLine, percentThroughSong, interpolationStep)
//            .draw(boundRect)
//        }
      }
    }
  }
}

@Serializable
data class AudioLinesData(
  var audio: Audio = Audio(),
  var sampleSize: Int = DefaultSampleSize,
  var interpolationStep: Double = 1.0,
  var linesToShow: Int = 200,
  var drawSize: Point = Point(0.5, 0.5),
  var drawCenter: Point = Point(0.5, 0.5),
  var amplitudeScale: Double = 1.0,
  var numLayers: Int = 1,
  var interpolationType: Interpolator1DType = CubicSpline1DType,
  var noise: Noise = Noise.DEFAULT,
  var drawBaseLines: Boolean = false,
  var percentThroughSong: Double = 0.5
) : PropData<AudioLinesData> {
  override fun bind() = tabs {
    tab("audio") {
      audioSelect(::audio)
      slider(::linesToShow, 1..2000)
      row(style = ControlStyle.Gray) {

        dropdown(::interpolationType)
        col {
          slider(::sampleSize, 10..20_000)
          slider(::interpolationStep, range = 1.0..100.0)
          toggle(::drawBaseLines)
        }
      }
      row(style = ControlStyle.Red) {
        sliderPair(
          ::drawSize,
          SliderPairArgs(0.0..2.0),
        )
        slider2D(::drawCenter, Slider2DArgs(0..1))
      }
      slider(::percentThroughSong, range = 0.0..1.0)
      slider(::amplitudeScale, range = 0.0..1.0)
      slider(::numLayers, range = 0..10)
    }
    tab("Noise") {
      style = ControlStyle.Red
      noisePanel(::noise)
    }
  }


  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = AudioLines().run()
