package sketches

import appletExtensions.draw.drawSquare
import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.degreeSlider
import controls.panels.panelext.dropdown
import controls.panels.panelext.intSlider
import controls.panels.panelext.sliderPair
import controls.props.PropData
import controls.props.types.PhotoProp
import coordinate.Deg
import coordinate.Point
import coordinate.Segment.Companion.toUnitVectorSegment
import geomerativefork.src.util.bound
import kotlinx.serialization.Serializable
import sketches.FilterType.Circles
import sketches.FilterType.Crosses
import sketches.FilterType.Crosses2
import sketches.FilterType.Crosses4
import sketches.FilterType.Lines
import sketches.FilterType.Squares
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.image.bounds
import util.image.get
import util.image.gradientAt
import util.tuple.and

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class PhotoFilter : LayeredCanvasSketch<PhotoFilterData, PhotoFilterLayerData>(
  "PhotoFilter",
  defaultGlobal = PhotoFilterData(),
  layerToDefaultTab = { PhotoFilterLayerData() },
  maxLayers = 1,
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (photo, sampleRate, filterType, objectSize, baseRotation, filterSize) = values.globalValues
    val (PhotoFilterTabField) = values.tabValues

    val image = photo.loadMemoized(this) ?: return

    val imageBounds = image
      .bounds
      .recentered(boundRect.pointAt(photo.imageCenter))

    if (photo.drawImage)
      image(image, imageBounds.topLeft.xf, imageBounds.topLeft.yf)

    image.bounds.forEachSampled(sampleRate.x, sampleRate.y) { imagePoint ->
      val canvasPoint = imagePoint + imageBounds.topLeft

      if (photo.shouldCrop && !photo.cropShape.contains(canvasPoint, boundRect))
        return@forEachSampled

      val lumAtP = image.get(imagePoint).red / 255.0

      val length = (objectSize.x..objectSize.y).atAmountAlong(lumAtP)
      if (length < 0) return@forEachSampled

      when (filterType) {
        Circles -> canvasPoint.draw(length)
        Crosses -> {
          val segment = image
            .gradientAt(imagePoint, filterSize)
            .toUnitVectorSegment(canvasPoint)
            .resizeCentered(length)
            .centeredWithSlope(baseRotation)
          listOf(segment, segment.centeredWithSlope(segment.slope + 90)).drawAsLine()
        }
        Crosses2 -> {
          val length1 = lumAtP.bound(0.0, 0.5) * 2 * length
          val length2 = (lumAtP.bound(0.5, 1.0) - 0.5) * 2 * length

          val segment = image
            .gradientAt(imagePoint, filterSize)
            .toUnitVectorSegment(canvasPoint)
            .centeredWithSlope(baseRotation)
          listOf(
            segment
              .resizeCentered(length1),
            segment
              .centeredWithSlope(segment.slope + 90)
              .resizeCentered(length2),
          )
            .filter { it.length > 1 }
            .drawAsLine()
        }
        Crosses4 -> {
          val length1 = lumAtP.bound(0.0, 0.25) * 4 * length
          val length2 = (lumAtP.bound(0.25, 0.5) - 0.25) * 4 * length
          val length3 = (lumAtP.bound(0.5, 0.75) - 0.5) * 4 * length
          val length4 = (lumAtP.bound(0.75, 1.0) - 0.75) * 4 * length

          val segment = image
            .gradientAt(imagePoint, filterSize)
            .toUnitVectorSegment(canvasPoint)
            .centeredWithSlope(baseRotation)
          listOf(
            segment
              .resizeCentered(length1),
            segment
              .centeredWithSlope(segment.slope + 90)
              .resizeCentered(length2),
            segment
              .centeredWithSlope(segment.slope + 45)
              .resizeCentered(length3),
            segment
              .centeredWithSlope(segment.slope + 135)
              .resizeCentered(length4),
          )
            .filter { it.length > 1 }
            .drawAsLine()
        }
        Squares -> drawSquare(canvasPoint, length, rotation = baseRotation)
        Lines -> image
          .gradientAt(imagePoint, filterSize)
          .toUnitVectorSegment(canvasPoint)
          .resizeCentered(length)
          .centeredWithSlope(baseRotation)
          .draw()
      }
    }
  }
}

@Serializable
data class PhotoFilterLayerData(
  var PhotoFilterTabField: Int = 1,
) : PropData<PhotoFilterLayerData> {
  override fun bind() = layerTab {
    intSlider(::PhotoFilterTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

enum class FilterType {
  Circles,
  Squares,
  Lines,
  Crosses,
  Crosses2,
  Crosses4,
}

@Serializable
data class PhotoFilterData(
  var photo: PhotoProp = PhotoProp(),
  var sampleRate: Point = Point(5, 5),
  var filterType: FilterType = Lines,
  var objectSize: Point = Point(0, 30),
  var baseRotation: Deg = Deg(0),
  var filterSize: Int = 1,
) : PropData<PhotoFilterData> {
  override fun bind() = tabs {
    tab("Photo") {
      panel(::photo)
    }

    tab("Filters") {
      tabStyle = TabStyle.Green
      style = ControlStyle.Blue
      dropdown(::filterType, style = ControlStyle.Gray)
      sliderPair(::sampleRate, 2.0..50.0, withLockToggle = true)
      sliderPair(::objectSize, 0.0..20.0 and 2.0..30.0)
      degreeSlider(::baseRotation)
      intSlider(::filterSize, 1..100)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PhotoFilter().run()
