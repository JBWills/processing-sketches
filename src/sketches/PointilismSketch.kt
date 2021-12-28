package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import coordinate.Point
import coordinate.transforms.TranslateTransform
import fastnoise.Noise
import fastnoise.toOpenCVMat
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.image.opencvMat.filters.vignetteFilter
import util.image.opencvMat.findContours
import util.image.opencvMat.getOr
import util.image.opencvMat.threshold
import util.numbers.bound
import util.numbers.times
import util.polylines.closed
import util.polylines.transform
import util.randomPoint


/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class PointillismSketch : SimpleCanvasSketch<PointillismData>("Pointillism", PointillismData()) {

  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (noise, threshold, vignetteAmount, drawThresholdShape, drawNoiseField, numPoints, numLayers, pointSize) = drawInfo.dataValues

    val matToScreen = TranslateTransform(boundRect.topLeft - Point(10, 10))
    val screenToMat = matToScreen.inverted()

    val noiseMatBounds = boundRect.expand(10)

    val noiseMat =
      noise
        .toOpenCVMat(noiseMatBounds)
        .vignetteFilter((1 - vignetteAmount), inPlace = true)

    if (drawNoiseField && !isRecording) {
      noiseMat.draw(noiseMatBounds.topLeft)
    }

    val noiseMatThreshold = noiseMat.threshold(threshold * 255)

    val noiseShape = noiseMatThreshold
      .findContours()
      .map { it.closed() }
      .transform(matToScreen)

    if (drawThresholdShape) {
      noiseShape
        .draw(boundRect)
      nextLayer()
    }

    var lastLayer = 0
    numPoints.times { pointIndex ->
      val layer = ((pointIndex / numPoints.toDouble()) * numLayers).toInt()
      if (layer != lastLayer) {
        nextLayer()
      }

      val point = boundRect.randomPoint()

      val percentToThreshold = (noiseMat.getOr(
        point.transform(screenToMat),
        0.0,
      ) / 255.0).bound(0.0..threshold) / threshold

      val shouldDraw = percentToThreshold >= random(1f).toDouble()

      if (shouldDraw) {
        point.draw(pointSize)
      }

      lastLayer = layer
    }
  }
}

@Serializable
data class PointillismData(
  var noise: Noise = Noise.DEFAULT,
  var threshold: Double = 0.5,
  var vignetteAmount: Double = 0.0,
  var drawThresholdShape: Boolean = true,
  var drawNoiseField: Boolean = true,
  var numPoints: Int = 5_000,
  var numLayers: Int = 1,
  var pointSize: Int = 3,
) : PropData<PointillismData> {
  override fun bind() = tabs {
    tab("Noise") {
      noisePanel(::noise)
      slider(::threshold)
      slider(::vignetteAmount)
      row {
        toggle(::drawThresholdShape)
        toggle(::drawNoiseField)
      }
    }

    tab("Points") {
      slider(::numPoints, 0..100_000)
      slider(::numLayers, 1..5)
      slider(::pointSize, 1..10)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PointillismSketch().run()
