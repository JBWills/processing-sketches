package sketches

import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.slider
import controls.props.PropData
import controls.props.types.PhotoProp
import controls.props.types.SpiralProp
import coordinate.Deg
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.image.pimage.bounds
import util.image.pimage.get
import util.polylines.mapWithLength

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class SpiralPhotoFilter : LayeredCanvasSketch<SpiralPhotoFilterData, SpiralPhotoFilterLayerData>(
  "SpiralPhotoFilter",
  defaultGlobal = SpiralPhotoFilterData(),
  layerToDefaultTab = { SpiralPhotoFilterLayerData() },
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
    }.mapWithLength { point, length ->
      val imagePoint =
        (point - imageBounds.topLeft).let { if (image.bounds.contains(it)) it else null }

      val lumAtP = imagePoint?.let { image.get(it).red / 255.0 } ?: 0.0
      val degTwo = Deg((length / spiralFreq))
      point + degTwo.unitVector * spiralAmp * lumAtP
    }.draw()
  }
}

@Serializable
data class SpiralPhotoFilterLayerData(
  var SpiralPhotoFilterTabField: Int = 1,
) : PropData<SpiralPhotoFilterLayerData> {
  override fun bind() = layerTab {
    slider(::SpiralPhotoFilterTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class SpiralPhotoFilterData(
  var photo: PhotoProp = PhotoProp(),
  var spiral: SpiralProp = SpiralProp(),
  var spiralFreq: Double = 100.0,
  var spiralAmp: Double = 10.0,
  var skew: Double = 0.0,
) : PropData<SpiralPhotoFilterData> {
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

fun main() = SpiralPhotoFilter().run()
