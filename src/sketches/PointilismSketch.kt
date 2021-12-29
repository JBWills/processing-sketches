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
import sketches.RandomizePositionType.EqualDistances
import sketches.RandomizePositionType.RandomDistances
import sketches.base.SimpleCanvasSketch
import util.image.opencvMat.bounds
import util.image.opencvMat.filters.vignetteFilter
import util.image.opencvMat.findContours
import util.image.opencvMat.getOr
import util.image.opencvMat.threshold
import util.iterators.deepMap
import util.numbers.bound
import util.numbers.sqrt
import util.polylines.closed
import util.polylines.transform
import util.randomDouble
import util.translatedRandomDirection


/**
 * Draw grayscale image using dots.
 */
class PointillismSketch : SimpleCanvasSketch<PointillismData>("Pointillism", PointillismData()) {

  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (inputData, pointsData) = drawInfo.dataValues


    val getShowPoint: ((p: Point) -> Boolean) = if (inputData.inputType == InputType.Noise) {
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
      { p ->
        val percentToThreshold = (noiseMat.getOr(
          p.transform(screenToMat),
          0.0,
        ) / 255.0).bound(0.0..inputData.threshold) / inputData.threshold

        percentToThreshold >= random(1f).toDouble()
      }
    } else {
      val photoMat =
        inputData.photo.loadMatMemoized()
//          ?.vignetteFilter((1 - inputData.vignetteAmount), inPlace = false)

      val imageBounds = photoMat
        .bounds
        .recentered(boundRect.pointAt(inputData.photo.imageCenter))

      if (inputData.photo.drawImage) {
        photoMat.draw(offset = imageBounds.topLeft)
      }

      { p: Point ->
        val percentToThreshold = (photoMat.getOr(
          p - imageBounds.topLeft,
          0.0,
        ) / 255.0).bound(0.0..inputData.threshold) / inputData.threshold

        percentToThreshold >= random(1f).toDouble()
      }
    }

    /*
     * w * h = numPX
     * h/w = ratioHW
     * h = ratioHW * w
     * w * (ratioHW * w) = numPX
     * w ^ 2 = numPix / ratioHW
     * w = sqrt(numPix / ratioHW)
     * h = ratioHW * sqrt(numPix / ratioHW)
     */
    val ratioHW = boundRect.height / boundRect.width
    val dotsX = (pointsData.numPoints.toDouble() / ratioHW).sqrt()
    val dotsY = ratioHW * dotsX

    boundRect
      .mapSampled(dotsX, dotsY) { p -> p }
      .deepMap { point ->
        val dist = when (pointsData.randomizePositionType) {
          RandomDistances -> randomDouble(0.0..pointsData.randomizePosition)
          EqualDistances -> pointsData.randomizePosition
        }
        val movedPoint = point.translatedRandomDirection(dist, pointsData.pointsSeed)

        if (boundRect.contains(movedPoint) && getShowPoint(movedPoint)) {
          movedPoint.draw(pointsData.pointSize)
        }
      }
  }
}

enum class InputType { Noise, Image }
enum class RandomizePositionType { EqualDistances, RandomDistances }

//@Serializable
//data class PointsLayer(
//  var color: Color = Color.BLACK,
//
//  )

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
  var pointsSeed: Int = 0,
  var randomizePosition: Double = 0.0,
  var randomizePositionType: RandomizePositionType = RandomDistances,
)

@Serializable
data class PointillismData(
  val inputData: InputData = InputData(),
  val pointsData: PointsData = PointsData(),
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
      slider(pointsData::numPoints, 0..1_000_000)
      slider(pointsData::numLayers, 1..5)
      slider(pointsData::pointSize, 1..10)
      slider(pointsData::pointsSeed, 1..10_000)
      row {
        dropdown(pointsData::randomizePositionType)
        slider(pointsData::randomizePosition, 0.0..100.0)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PointillismSketch().run()
