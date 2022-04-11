package controls.props.types

import arrow.core.memoize
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.ColorPicker.Companion.colorPicker
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.imageSelect
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import coordinate.transforms.ScaleTransform
import coordinate.transforms.ShapeTransform
import coordinate.transforms.TranslateTransform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import util.base.doIf
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvMat.bounds
import util.image.opencvMat.copy
import util.image.opencvMat.filters.clamp
import util.image.opencvMat.filters.dithering.DitherType
import util.image.opencvMat.filters.dithering.DitherType.Burkes
import util.image.opencvMat.filters.dithering.ditherByColor
import util.image.opencvMat.filters.invert
import util.image.opencvMat.flags.ImreadFlags.ImreadGrayscale
import util.image.opencvMat.flags.ImreadFlags.ImreadUnchanged
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.loadImageMatMemo
import util.image.opencvMat.scaleByLargestDimension
import util.io.serialization.ColorSerializer
import java.awt.Color

@Serializable
data class DitherProps(
  var shouldDither: Boolean = false,
  @Serializable(with = ColorSerializer::class)
  var color: Color = Color.WHITE,
  var ditherType: DitherType = Burkes,
  var maxDiff: Double = 15.0
)

@Serializable
data class ImageTransformProps(
  var imageBlackPoint: Int = 0,
  var imageWhitePoint: Int = 255,
  var blurRadius: Double = 3.0,
  var invert: Boolean = false,
  var grayscale: Boolean = true,
  var dither: DitherProps = DitherProps(),
)

@Serializable
data class PhotoMatProp(
  var photoFile: String = "",
  var imageCenter: Point = Point.Half,
  var imageSizeFine: Double = 500.0,
  var imageSizeMul: Double = 1.0,
  var drawImage: Boolean = true,
  var transformProps: ImageTransformProps = ImageTransformProps()
) : PropData<PhotoMatProp> {
  constructor(
    s: PhotoMatProp,
    photoFile: String? = null,
    imageCenter: Point? = null,
    imageSizeFine: Double? = null,
    imageSizeMul: Double? = null,
    drawImage: Boolean? = null,
    transformProps: ImageTransformProps? = null,
  ) : this(
    photoFile ?: s.photoFile,
    imageCenter ?: s.imageCenter,
    imageSizeFine ?: s.imageSizeFine,
    imageSizeMul ?: s.imageSizeMul,
    drawImage ?: s.drawImage,
    transformProps ?: s.transformProps,
  )

  fun getMatBounds(mat: Mat, boundRect: BoundRect) =
    mat.bounds.scale(Point(imageSizeMul), boundRect.pointAt(imageCenter + 0.5))

  fun getScreenToMatTransform(mat: Mat, boundRect: BoundRect): ShapeTransform {
    val matScreenBounds = getMatBounds(mat, boundRect)

    return TranslateTransform(-matScreenBounds.topLeft)
      .addTransform(ScaleTransform(Point(1 / imageSizeMul), Point.Zero))
  }

  fun getMatToScreenTransform(mat: Mat, boundRect: BoundRect): ShapeTransform {
    return getScreenToMatTransform(mat, boundRect).inverted()
  }

  fun loadMatMemoized(): Mat? = _loadAndTransformMat(
    photoFile,
    imageSizeFine,
    transformProps.copy(),
  )

  override fun toSerializer() = serializer()

  override fun clone() = PhotoMatProp(this)

  override fun bind(): List<ControlTab> = tabs {
    tab("Photo") {
      row {
        heightRatio = 0.5
        imageSelect(::photoFile)
        toggle(::drawImage).withWidth(0.25)
      }

      row {
        heightRatio = 3
        slider2D(::imageCenter, -1..1)
        col {
          widthRatio = 0.3

          button("Center image") { imageCenter = Point(0, 0); updateControls() }
        }
      }

      row {
        slider(::imageSizeFine, 2..4000)
        slider(::imageSizeMul, 1..10)
      }
    }

    tab("filters") {
      slider(transformProps::blurRadius, 0..50)

      col {
        style = ControlStyle.Gray
        row {
          slider(transformProps::imageBlackPoint, 0..255)
          slider(transformProps::imageWhitePoint, 0..255)
        }
        row {
          toggle(transformProps::invert, style = ControlStyle.Black)
          toggle(transformProps::grayscale, style = ControlStyle.Black)
        }
      }

      row {
        toggle(transformProps.dither::shouldDither)
        dropdown(transformProps.dither::ditherType)
        slider(transformProps.dither::maxDiff, 0.0..100.0)
        colorPicker(transformProps.dither::color)
      }
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
  val (imageBlackPoint, imageWhitePoint, blurRadius, invert, grayscale, dither) = imageTransform

  loadImageMatMemo(path, if (grayscale) ImreadGrayscale else ImreadUnchanged)
    ?.copy()
    ?.scaleByLargestDimension(imageSize)
    ?.clamp(
      min = imageBlackPoint.toDouble(),
      max = imageWhitePoint.toDouble(),
      inPlace = true,
    )
    ?.doIf(blurRadius > 1) {
      it.gaussianBlur(blurRadius.toInt(), inPlace = true)
    }
    ?.doIf(invert) { it.invert(inPlace = true) }
    ?.doIf(dither.shouldDither) {
      it.ditherByColor(
        dither.ditherType,
        dither.color,
        dither.maxDiff,
        it.getFormat(),
        inPlace = false,
      )
    }
}
  .memoize()
