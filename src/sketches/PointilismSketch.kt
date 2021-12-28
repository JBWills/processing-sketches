package sketches

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import controls.props.types.PhotoProp
import coordinate.Point
import coordinate.transforms.TranslateTransform
import fastnoise.Noise
import fastnoise.toOpenCVMat
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import sketches.InputType.Image
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
    val (inputData, pointsData) = drawInfo.dataValues

    val matToScreen = TranslateTransform(boundRect.topLeft - Point(10, 10))
    val screenToMat = matToScreen.inverted()

    val noiseMatBounds = boundRect.expand(10)

    val noiseMat =
      inputData.noise
        .toOpenCVMat(noiseMatBounds)
        .vignetteFilter((1 - inputData.vignetteAmount), inPlace = true)

    if (inputData.drawNoiseField && !isRecording) {
      noiseMat.draw(noiseMatBounds.topLeft)
    }

    val noiseMatThreshold = noiseMat.threshold(inputData.threshold * 255)

    val noiseShape = noiseMatThreshold
      .findContours()
      .map { it.closed() }
      .transform(matToScreen)

    if (inputData.drawThresholdShape) {
      noiseShape
        .draw(boundRect)
      nextLayer()
    }

    var lastLayer = 0
    pointsData.numPoints.times { pointIndex ->
      val layer = ((pointIndex / pointsData.numPoints.toDouble()) * pointsData.numLayers).toInt()
      if (layer != lastLayer) {
        nextLayer()
      }

      val point = boundRect.randomPoint()

      val percentToThreshold = (noiseMat.getOr(
        point.transform(screenToMat),
        0.0,
      ) / 255.0).bound(0.0..inputData.threshold) / inputData.threshold

      val shouldDraw = percentToThreshold >= random(1f).toDouble()

      if (shouldDraw) {
        point.draw(pointsData.pointSize)
      }

      lastLayer = layer
    }
  }
}

enum class InputType {
  Noise,
  Image
}

@Serializable
data class InputData(
  var inputType: InputType = InputType.Noise,
  var noise: Noise = Noise.DEFAULT,
  var threshold: Double = 0.5,
  var vignetteAmount: Double = 0.0,
  var photo: PhotoProp = PhotoProp(),
  var drawThresholdShape: Boolean = true,
  var drawNoiseField: Boolean = true,
)

@Serializable
data class PointsData(
  var numPoints: Int = 5_000,
  var numLayers: Int = 1,
  var pointSize: Int = 3,
  var pointsSeed: Int = 0
)

@Serializable
data class PointillismData(
  var inputData: InputData = InputData(),
  var pointsData: PointsData = PointsData(),
) : PropData<PointillismData> {
  override fun bind() = tabs {
    tab("input") {
      dropdown(inputData::inputType) { updateControls() }

      slider(inputData::threshold)
      slider(inputData::vignetteAmount)
    }

    when (inputData.inputType) {
      InputType.Noise -> tab("Noise") {
        noisePanel(inputData::noise)
        row {
          toggle(inputData::drawThresholdShape)
          toggle(inputData::drawNoiseField)
        }
      }

      Image -> tab("Image") {
        panel(inputData::photo)
      }
    }

    tab("Points") {
      slider(pointsData::numPoints, 0..100_000)
      slider(pointsData::numLayers, 1..5)
      slider(pointsData::pointSize, 1..10)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PointillismSketch().run()
