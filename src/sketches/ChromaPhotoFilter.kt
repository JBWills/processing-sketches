package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PhotoProp
import controls.props.types.SpiralProp
import coordinate.Deg
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.image.pimage.bounds
import util.image.pimage.get
import util.polylines.iterators.walkWithCursor

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class ChromaPhotoFilter :
  LayeredCanvasSketch<ChromaPhotoFilterData, ChromaPhotoFilterLayerData>(
    "ChromaPhotoFilter",
    defaultGlobal = ChromaPhotoFilterData(),
    layerToDefaultTab = { ChromaPhotoFilterLayerData() },
    maxLayers = 1,
  ) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (photo, spiral, spiralFreq, spiralAmp, _) = layerInfo.globalValues

    val image = photo.loadMemoized(this) ?: return

    val imageBounds = image
      .bounds
      .recentered(boundRect.pointAt(photo.imageCenter))

    if (photo.drawImage)
      image(image, imageBounds.topLeft.xf, imageBounds.topLeft.yf)

    spiral.spiral(boundRect) { _, _, _, p ->
      p
    }.walkWithCursor(2) { cursor ->
      val point = cursor.point
      val length = cursor.distance
      val imagePoint =
        (point - imageBounds.topLeft).let { if (image.bounds.contains(it)) it else null }

      val lumAtP = imagePoint?.let { image.get(it).red / 255.0 } ?: 0.0
//      val sinVal =
//        ((length / spiralFreq).sin() + (length / spiralFreq).cos())
//      val moveAmount = sinVal * spiralAmp * lumAtP
//      val deg = Segment(boundRect.pointAt(spiral.origin), point).slope
      val degTwo = Deg((length / spiralFreq))
//      val moveVector =
//        deg.unitVector * moveAmount + (deg.plus(90).unitVector * skew * ((-2 * length) / spiralFreq).sin() * lumAtP)
      point + degTwo.unitVector * spiralAmp * lumAtP
    }.draw()
  }
}

@Serializable
data class ChromaPhotoFilterLayerData(
  var ChromaPhotoFilterTabField: Int = 1,
) : PropData<ChromaPhotoFilterLayerData> {
  override fun bind() = layerTab {
    slider(::ChromaPhotoFilterTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class ChromaPhotoFilterData(
  var photo: PhotoProp = PhotoProp(),
  var spiral: SpiralProp = SpiralProp(),
  var spiralFreq: Double = 100.0,
  var spiralAmp: Double = 10.0,
  var skew: Double = 0.0,
) : PropData<ChromaPhotoFilterData> {
  override fun bind() = tabs {
    tab("Photo") {
      panel(::photo)
    }

    tab("Spiral") {
      tabStyle = TabStyle.Green
      style = ControlStyle.Blue

      panel(::spiral)

      row {
        slider(::spiralFreq, 0.01..2.0)
        slider(::spiralAmp, 0.0..100.0)
      }

      slider(::skew, -100..100)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ChromaPhotoFilter().run()
