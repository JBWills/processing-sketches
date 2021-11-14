package sketches

import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.audioSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Point
import data.Audio
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.audio.DefaultSampleSize
import util.map

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
    val (audio, sampleSize, linesToShow, drawSize, drawCenter, amplitudeScale, numLayers) = drawInfo.dataValues

    val features = audio.getFeatures(sampleSize) ?: return

    val pressureSamples = features.pressures.size
    val samplesPerLine = pressureSamples / linesToShow
    val linesBound = centeredRect(boundRect.pointAt(drawCenter), boundRect.size * drawSize)

    linesToShow.map { lineIndex ->
      samplesPerLine.map { sampleIndex ->
        val basePoint = linesBound.topLeft + Point(
          (sampleIndex.toDouble() / samplesPerLine) * linesBound.width,
          (lineIndex.toDouble() / linesToShow) * linesBound.height,
        )

        basePoint.addY(
          features.pressures[lineIndex * samplesPerLine + sampleIndex] * amplitudeScale,
        )
      }
    }.draw(boundRect)
  }
}

@Serializable
data class AudioLinesData(
  var audio: Audio = Audio(),
  var sampleSize: Int = DefaultSampleSize,
  var linesToShow: Int = 200,
  var drawSize: Point = Point(0.5, 0.5),
  var drawCenter: Point = Point(0.5, 0.5),
  var amplitudeScale: Double = 1.0,
  var numLayers: Int = 1
) : PropData<AudioLinesData> {
  override fun bind() = singleTab("audio") {
    audioSelect(::audio)
    col {
      slider(::sampleSize, 10..2048)
      slider(::linesToShow, 1..2000)
    }
    col {
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
