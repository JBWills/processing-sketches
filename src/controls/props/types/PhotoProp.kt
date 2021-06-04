package controls.props.types

import BaseSketch
import arrow.core.memoize
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.imageSelect
import controls.panels.panelext.intSlider
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.toggle
import controls.props.PropData
import coordinate.Point
import kotlinx.serialization.Serializable
import processing.core.PImage
import util.ZeroToOne
import util.doIf
import util.image.blurred
import util.image.inverted
import util.image.luminance
import util.image.scaleByLargestDimension
import util.image.threshold
import util.io.loadImageMemo
import util.tuple.and


@Serializable
data class PhotoProp(
  var photoFile: String = "",
  var imageCenter: Point = Point.Half,
  var imageSize: Double = 500.0,
  var imageBlackPoint: Int = 0,
  var imageWhitePoint: Int = 255,
  var drawImage: Boolean = true,
  var blurRadius: Double = 3.0,
  var invert: Boolean = false,
  var cropShape: ShapeProp = ShapeProp(),
  var shouldCrop: Boolean = false,
) : PropData<PhotoProp> {
  constructor(
    s: PhotoProp,
    photoFile: String? = null,
    imageCenter: Point? = null,
    imageSize: Double? = null,
    imageBlackPoint: Int? = null,
    imageWhitePoint: Int? = null,
    drawImage: Boolean? = null,
    blurRadius: Double? = null,
    invert: Boolean? = null,
    cropShape: ShapeProp? = null,
    shouldCrop: Boolean? = null,
  ) : this(
    photoFile ?: s.photoFile,
    imageCenter ?: s.imageCenter,
    imageSize ?: s.imageSize,
    imageBlackPoint ?: s.imageBlackPoint,
    imageWhitePoint ?: s.imageWhitePoint,
    drawImage ?: s.drawImage,
    blurRadius ?: s.blurRadius,
    invert ?: s.invert,
    cropShape ?: s.cropShape,
    shouldCrop ?: s.shouldCrop,
  )

  fun loadMemoized(sketch: BaseSketch): PImage? = _transformImage(
    sketch.loadImageMemo(photoFile),
    imageSize,
    imageBlackPoint to imageWhitePoint,
    invert,
    blurRadius,
  )

  override fun toSerializer() = serializer()

  override fun clone() = PhotoProp(this)

  override fun bind(): List<ControlTab> = singleTab("Photo") {
    row {
      imageSelect(::photoFile)

      toggle(::drawImage).withWidth(0.25)
    }

    slider(::blurRadius, 0..50)

    row {
      slider2D(::imageCenter, ZeroToOne and ZeroToOne)
      slider(::imageSize, 2..2000)
    }

    row {
      style = ControlStyle.Gray
      intSlider(::imageBlackPoint, 0..255)
      intSlider(::imageWhitePoint, 0..255)
      toggle(::invert, style = ControlStyle.Black)
    }

    toggle(::shouldCrop)
    panel(::cropShape, style = ControlStyle.Red)
  }
}

private val _transformImage = { img: PImage?,
                                imageSize: Double,
                                imageBlackAndWhitePoint: Pair<Int, Int>,
                                invert: Boolean,
                                blurRadius: Double ->
  img
    ?.scaleByLargestDimension(imageSize)
    ?.luminance()
    ?.threshold(min = imageBlackAndWhitePoint.first, max = imageBlackAndWhitePoint.second)
    ?.doIf(blurRadius > 1) { it.blurred(blurRadius) }
    ?.doIf(invert) { it.inverted() }
}.memoize()