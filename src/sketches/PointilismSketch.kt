package sketches

import appletExtensions.withStyle
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import controls.props.types.PenProp
import controls.props.types.PhotoMatProp
import coordinate.Point
import coordinate.util.mapPoints
import fastnoise.Noise
import fastnoise.toOpenCVMat
import kotlinx.serialization.Serializable
import sketches.InputType.Image
import sketches.RandomizePositionType.EqualDistances
import sketches.RandomizePositionType.RandomDistances
import sketches.base.SimpleCanvasSketch
import util.image.opencvMat.filters.vignetteFilter
import util.image.opencvMat.getOr
import util.iterators.flatMapNonNull
import util.layers.LayerSVGConfig
import util.numbers.bound
import util.rand
import util.randomDouble
import util.translatedRandomDirection


/**
 * Draw grayscale image using dots.
 */
class PointillismSketch : SimpleCanvasSketch<PointillismData>("Pointillism", PointillismData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (inputData, pointsData, pen) = drawInfo.dataValues

    val (inputMat, inputBounds) = when (inputData.inputType) {
      InputType.Noise -> {
        val bounds = boundRect.expand(10)
        val mat = inputData.noise
          .toOpenCVMat(bounds)
          .vignetteFilter((1 - inputData.vignetteAmount), inPlace = true)
        mat to bounds
      }
      Image -> {
        inputData.photo.loadMatMemoized()?.let { mat ->
          val bounds = inputData.photo.getMatBounds(mat, boundRect)
          mat to bounds
        } ?: return
      }
    }

    fun shouldShowPoint(p: Point): Boolean {
      val percentToThreshold =
        inputMat.getOr(p - inputBounds.topLeft, 0.0)
          .div(255.0 * inputData.threshold)
          .bound(0.0..1.0)

      return percentToThreshold >= rand(pointsData.pointsSeed)
    }

    withStyle(pen.style) {
      boundRect
        .boundsIntersection(inputBounds)
        ?.mapPoints(pointsData.numPoints)
        ?.flatMapNonNull { point ->
          val dist = when (pointsData.randomizePositionType) {
            RandomDistances -> randomDouble(
              0.0..pointsData.randomizePosition,
              pointsData.pointsSeed,
            )
            EqualDistances -> pointsData.randomizePosition
          }
          val movedPoint = point.translatedRandomDirection(dist, pointsData.pointsSeed)

          if (shouldShowPoint(movedPoint)) movedPoint else null
        }
        ?.drawPoints(pointsData.pointSize)
    }
  }
}

enum class InputType { Noise, Image }
enum class RandomizePositionType { EqualDistances, RandomDistances }

@Serializable
data class InputData(
  var inputType: InputType = InputType.Noise,
  var noise: Noise = Noise.DEFAULT,
  var threshold: Double = 0.5,
  var vignetteAmount: Double = 0.0,
  var photo: PhotoMatProp = PhotoMatProp(),
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
  var pen: PenProp = PenProp()
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

    tab("Pen") {
      panel(::pen)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PointillismSketch().run()
