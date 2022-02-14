package controls.props.types

import arrow.core.memoize
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.imageSelect
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import util.base.doIf
import util.image.opencvMat.bounds
import util.image.opencvMat.copy
import util.image.opencvMat.filters.clamp
import util.image.opencvMat.filters.invert
import util.image.opencvMat.flags.ImreadFlags.ImreadGrayscale
import util.image.opencvMat.flags.ImreadFlags.ImreadUnchanged
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.loadImageMatMemo
import util.image.opencvMat.scaleByLargestDimension

@Serializable
data class ImageTransformProps(
  var imageBlackPoint: Int = 0,
  var imageWhitePoint: Int = 255,
  var blurRadius: Double = 3.0,
  var invert: Boolean = false,
  var grayscale: Boolean = true,
)

@Serializable
data class PhotoMatProp(
  var photoFile: String = "",
  var imageCenter: Point = Point.Half,
  var imageSize: Double = 500.0,
  var drawImage: Boolean = true,
  var transformProps: ImageTransformProps = ImageTransformProps()
) : PropData<PhotoMatProp> {
  constructor(
    s: PhotoMatProp,
    photoFile: String? = null,
    imageCenter: Point? = null,
    imageSize: Double? = null,
    drawImage: Boolean? = null,
    transformProps: ImageTransformProps? = null,
  ) : this(
    photoFile ?: s.photoFile,
    imageCenter ?: s.imageCenter,
    imageSize ?: s.imageSize,
    drawImage ?: s.drawImage,
    transformProps ?: s.transformProps,
  )

  fun getMatBounds(mat: Mat, boundRect: BoundRect) =
    mat.bounds.recentered(boundRect.pointAt(imageCenter))

  fun getScreenToMatTransform(mat: Mat, boundRect: BoundRect): (Point) -> Point {
    val matScreenBounds = getMatBounds(mat, boundRect)

    return { p -> p - matScreenBounds.topLeft }
  }

  fun getMatToScreenTransform(mat: Mat, boundRect: BoundRect): (Point) -> Point {
    val matScreenBounds = getMatBounds(mat, boundRect)

    return { p -> p + matScreenBounds.topLeft }
  }

  fun loadMatMemoized(): Mat? = _loadAndTransformMat(
    photoFile,
    imageSize,
    transformProps.copy(),
  )

  override fun toSerializer() = serializer()

  override fun clone() = PhotoMatProp(this)

  override fun bind(): List<ControlTab> = singleTab("Photo") {
    row {
      imageSelect(::photoFile)

      toggle(::drawImage).withWidth(0.25)
    }

    row {
      slider2D(::imageCenter, 0..2)
      slider(::imageSize, 2..4000)
    }

    slider(transformProps::blurRadius, 0..50)

    row {
      style = ControlStyle.Gray
      slider(transformProps::imageBlackPoint, 0..255)
      slider(transformProps::imageWhitePoint, 0..255)
      toggle(transformProps::invert, style = ControlStyle.Black)
      toggle(transformProps::grayscale, style = ControlStyle.Black)
    }
  }
}

private val _loadAndTransformMat: (
  String,
  Double,
  ImageTransformProps
) -> Mat? = {
    path: String,
    imageSize: Double,
    imageTransform: ImageTransformProps,
  ->
  loadImageMatMemo(path, if (imageTransform.grayscale) ImreadGrayscale else ImreadUnchanged)
    ?.copy()
    ?.scaleByLargestDimension(imageSize)
    ?.clamp(
      min = imageTransform.imageBlackPoint.toDouble(),
      max = imageTransform.imageWhitePoint.toDouble(),
      inPlace = true,
    )
    ?.doIf(imageTransform.blurRadius > 1) {
      it.gaussianBlur(imageTransform.blurRadius.toInt(), inPlace = true)
    }
    ?.doIf(imageTransform.invert) { it.invert(inPlace = true) }
}
  .memoize()
