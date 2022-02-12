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
import util.image.opencvMat.filters.clamp
import util.image.opencvMat.filters.invert
import util.image.opencvMat.flags.ImreadFlags
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.loadImageMatMemo
import util.image.opencvMat.scaleByLargestDimension

@Serializable
data class PhotoMatProp(
  var photoFile: String = "",
  var imageCenter: Point = Point.Half,
  var imageSize: Double = 500.0,
  var imageBlackPoint: Int = 0,
  var imageWhitePoint: Int = 255,
  var drawImage: Boolean = true,
  var blurRadius: Double = 3.0,
  var invert: Boolean = false,
) : PropData<PhotoMatProp> {
  constructor(
    s: PhotoMatProp,
    photoFile: String? = null,
    imageCenter: Point? = null,
    imageSize: Double? = null,
    imageBlackPoint: Int? = null,
    imageWhitePoint: Int? = null,
    drawImage: Boolean? = null,
    blurRadius: Double? = null,
    invert: Boolean? = null,
  ) : this(
    photoFile ?: s.photoFile,
    imageCenter ?: s.imageCenter,
    imageSize ?: s.imageSize,
    imageBlackPoint ?: s.imageBlackPoint,
    imageWhitePoint ?: s.imageWhitePoint,
    drawImage ?: s.drawImage,
    blurRadius ?: s.blurRadius,
    invert ?: s.invert,
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
    imageBlackPoint to imageWhitePoint,
    invert,
    blurRadius,
  )

  override fun toSerializer() = serializer()

  override fun clone() = PhotoMatProp(this)

  override fun bind(): List<ControlTab> = singleTab("Photo") {
    row {
      imageSelect(::photoFile)

      toggle(::drawImage).withWidth(0.25)
    }

    slider(::blurRadius, 0..50)

    row {
      slider2D(::imageCenter, 0..2)
      slider(::imageSize, 2..4000)
    }

    row {
      style = ControlStyle.Gray
      slider(::imageBlackPoint, 0..255)
      slider(::imageWhitePoint, 0..255)
      toggle(::invert, style = ControlStyle.Black)
    }
  }
}

private val _loadAndTransformMat: (
  String,
  Double,
  Pair<Int, Int>,
  Boolean,
  Double
) -> Mat? = {
    path: String,
    imageSize: Double,
    imageBlackAndWhitePoint: Pair<Int, Int>,
    invert: Boolean,
    blurRadius: Double,
  ->
  loadImageMatMemo(path, ImreadFlags.ImreadGrayscale)
    ?.scaleByLargestDimension(imageSize)
    ?.clamp(
      min = imageBlackAndWhitePoint.first.toDouble(),
      max = imageBlackAndWhitePoint.second.toDouble(),
    )
    ?.doIf(blurRadius > 1) { it.gaussianBlur(blurRadius.toInt()) }
    ?.doIf(invert) { it.invert(inPlace = false) }
}
  .memoize()
