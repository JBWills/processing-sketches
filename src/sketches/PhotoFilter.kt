package sketches

import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PhotoProp
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.image.bounds
import util.image.get
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
    val (
      photo,
      sampleRate,
      circleSizes,
    ) = values.globalValues
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

      canvasPoint.drawPoint((circleSizes.x..circleSizes.y).atAmountAlong(lumAtP))
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

@Serializable
data class PhotoFilterData(
  var photo: PhotoProp = PhotoProp(),
  var sampleRate: Point = Point(5, 5),
  var circleSizes: Point = Point(0, 30),
) : PropData<PhotoFilterData> {
  override fun bind() = tabs {
    tab("Photo") {
      panel(::photo)
    }

    tab("Filters") {
      tabStyle = TabStyle.Green
      style = ControlStyle.Blue
      sliderPair(::sampleRate, 2.0..50.0, withLockToggle = true)
      sliderPair(::circleSizes, 0.0..20.0 and 2.0..30.0)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = PhotoFilter().run()
