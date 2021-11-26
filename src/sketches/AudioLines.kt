package sketches

import appletExtensions.withStroke
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.audioSelect
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Point
import data.AmplitudeLine
import data.Audio
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.audio.DefaultSampleSize
import util.interpolation.Interpolator1DType
import util.interpolation.Interpolator1DType.CubicSpline1DType
import util.iterators.mapDoubleArray
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
  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (audio, sampleSize, interpolationStep, linesToShow, drawSize, drawCenter, amplitudeScale, numLayers, interpolationType, noise, drawBaseLines) = drawInfo.dataValues

    val features = audio.getFeatures(sampleSize) ?: return

    val pressureSamples = features.pressures.size
    val samplesPerLine = pressureSamples / linesToShow
    val linesBound = centeredRect(boundRect.pointAt(drawCenter), boundRect.size * drawSize)

    val amplitudeLines = linesToShow.map { lineIndex ->
      val sampleIndex = (lineIndex * samplesPerLine)
      val pressures = features
        .pressuresFrom(sampleIndex..(sampleIndex + samplesPerLine))
        .mapDoubleArray { it * amplitudeScale }
      AmplitudeLine(pressures, interpolationType.create(), scaleFactor = amplitudeScale)
    }

    numLayers.map { layerIndex ->
      newLayerStyled(stroke = Colors[layerIndex % Colors.size]) {
        linesToShow.map { lineIndex ->
          val amplitudes = amplitudeLines[lineIndex]

          val baseBounds = BoundRect(
            linesBound.topLeft + Point(0, (lineIndex.toDouble() / linesToShow) * linesBound.height),
            width = linesBound.width,
            height = 0,
          )

          val baseLine = baseBounds.topSegment.warped(noise.with(seed = noise.seed + layerIndex))

          if (drawBaseLines) {
            withStroke(Color.YELLOW) { baseLine.draw(boundRect) }
          }

          amplitudes.interpolateAlong(baseLine, interpolationStep) { point, transformedPoint ->
            val diff = transformedPoint - point
            val diffOffset = diff + (diff.normalized * amplitudes.meanAmplitude)

            point + diffOffset
          }.draw(boundRect)
        }
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
        sliderPair(::drawSize, 0.0..2.0)
        slider2D(::drawCenter, 0..1 to 0..1)
      }
      slider(::amplitudeScale, range = 0..100)
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
