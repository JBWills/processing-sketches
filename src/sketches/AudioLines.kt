package sketches

import appletExtensions.withStroke
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.audioSelect
import controls.panels.panelext.dropdown
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Point
import data.AmplitudeLine
import data.Audio
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.audio.DefaultSampleSize
import util.interpolation.Interpolator1D
import util.interpolation.Interpolator1D.CubicSpline1D
import util.iterators.mapDoubleArray
import util.numbers.map
import util.polylines.length
import util.polylines.translated
import java.awt.Color

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class AudioLines : SimpleCanvasSketch<AudioLinesData>(
  "AudioLines",
  defaultData = AudioLinesData(),
) {
  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (audio, sampleSize, interpolationStep, linesToShow, drawSize, drawCenter, amplitudeScale, numLayers, interpolationType, interpolationSpread) = drawInfo.dataValues

    val features = audio.getFeatures(sampleSize) ?: return

    val pressureSamples = features.pressures.size
    val samplesPerLine = pressureSamples / linesToShow
    val linesBound = centeredRect(boundRect.pointAt(drawCenter), boundRect.size * drawSize)

    linesToShow.map { lineIndex ->
      val sampleIndex = (lineIndex * samplesPerLine)
      val pressures = features
        .pressuresFrom(sampleIndex..(sampleIndex + samplesPerLine))
        .mapDoubleArray { it * amplitudeScale }
      val amplitudeLine = AmplitudeLine(pressures, interpolationType, interpolationSpread)

      val baseBounds = BoundRect(
        linesBound.topLeft + Point(0, (lineIndex.toDouble() / linesToShow) * linesBound.height),
        width = linesBound.width,
        height = 0,
      )

      val baseLine = listOf(baseBounds.topLeft, baseBounds.topRight)

      withStroke(Color.PINK) {
        amplitudeLine
          .getUninterpolatedLine(baseLine.length)
          .translated(baseBounds.topLeft)
          .draw(boundRect)
      }

      amplitudeLine.interpolateAlong(baseLine, step = interpolationStep) { old, new ->
        val diff = new - old
        old + diff.withY(diff.y * amplitudeScale)
      }
    }.draw(boundRect)
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
  var interpolationType: Interpolator1D = CubicSpline1D,
  var interpolationSpread: Double = 1.0,
) : PropData<AudioLinesData> {
  override fun bind() = singleTab("audio") {
    audioSelect(::audio)
    slider(::linesToShow, 1..2000)
    row(style = ControlStyle.Gray) {

      dropdown(::interpolationType)
      col {
        slider(::interpolationSpread, 1..50)
        slider(::sampleSize, 10..20_000)
        slider(::interpolationStep, range = 1.0..100.0)
      }
    }
    row(style = ControlStyle.Red) {
      sliderPair(::drawSize, 0.0..2.0)
      slider2D(::drawCenter, 0..1 to 0..1)
    }
    slider(::amplitudeScale, range = 0..100)
    slider(::numLayers, range = 0..10)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = AudioLines().run()
