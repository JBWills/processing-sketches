package controls.props.types

import BaseSketch
import appletExtensions.background
import appletExtensions.createGraphicsAndDraw
import appletExtensions.createImage
import appletExtensions.draw
import appletExtensions.draw.circle
import appletExtensions.draw.shape
import appletExtensions.isMouseHovering
import appletExtensions.mouseLocation
import appletExtensions.withDraw
import appletExtensions.withDrawToImage
import appletExtensions.withStyle
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import controls.props.types.BrushType.Brush
import controls.props.types.BrushType.Bucket
import controls.props.types.BrushType.Eraser
import controls.props.types.BrushType.Gradient
import controls.props.types.BrushType.Line
import coordinate.Circ
import coordinate.Point
import interfaces.listeners.MouseDragListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.opencv.core.Mat
import processing.core.PGraphics
import processing.core.PImage
import util.base.lerp
import util.base.with
import util.base.withAlpha
import util.base.withAlphaDouble
import util.image.ImageFormat.Gray
import util.image.ImageFormat.Rgb
import util.image.ImageFormat.RgbaOpenCV
import util.image.blurAlpha
import util.image.converted
import util.image.opencvMat.asDisplayAlpha
import util.image.opencvMat.toMat
import util.image.opencvMat.toPImage
import util.image.pimage.overlay
import util.io.serialization.MatSerializer
import util.polylines.PolyLine
import util.print.CustomPx
import util.print.StrokeJoin
import util.print.Style
import java.awt.Color

enum class BrushType {
  Brush,
  Eraser,
  Bucket,
  Gradient,
  Line,
  ;
}

@Serializable
data class BrushProp(
  var size: Double = 10.0,
  var feather: Double = 0.0,
  var intensity: Double = 1.0,
  var showMask: Boolean = false,
  var brushType: BrushType = Brush,
) : PropData<BrushProp> {

  @Serializable(with = MatSerializer::class)
  var latestAlphaMat: Mat? = null

  @Transient
  var latestMaskDisplay: PImage? = null

  @Transient
  var alphaMask: PGraphics? = null

  @Transient
  val mouseDragListener = MouseDragListener()

  @Transient
  var hasBoundDragListener = false

  override fun toSerializer() = serializer()

  override fun clone() = copy()

  override fun bind(): List<ControlTab> = singleTab("BrushProp") {
    row {
      col {
        button("Clear") {
          getNonNullAlphaMask(this)
            .overlayARGBImage(createImage(format = RgbaOpenCV) { background(Color.black) })
          markDirty()
        }
        toggle(::showMask, shouldMarkDirty = false)
        dropdown(::brushType, shouldMarkDirty = false)
      }

      col {
        slider(::size, 0.0..700.0, shouldMarkDirty = false)
        slider(::feather, 0.0..1.0, shouldMarkDirty = false)
        slider(::intensity, 0.0..1.0, shouldMarkDirty = false)
      }
    }
  }

  private val featherPx: Double get() = feather * size

  private val brushColor: Color
    get() = when (brushType) {
      Brush -> Color.WHITE.withAlphaDouble(intensity)
      Eraser -> Color.BLACK.withAlphaDouble(intensity)
      Bucket -> listOf(Color.BLACK, Color.WHITE).lerp(intensity)
      Gradient -> listOf(Color.BLACK, Color.WHITE).lerp(intensity)
      Line -> Color.WHITE.withAlphaDouble(intensity)
    }

  private val brushStyle: Style
    get() = Style(
      color = brushColor,
      weight = CustomPx(size),
      join = StrokeJoin.Round,
      noFill = true,
    )

  private val brushCursorStyle: Style
    get() = Style(
      color = Color.white.withAlpha(128),
      noFill = true,
      weight = CustomPx(3),
    )

  private val brushFeatherCursorStyle: Style
    get() = Style(
      color = Color.white.withAlpha(100),
      weight = CustomPx(1.5),
      noFill = true,
    )

  private fun PGraphics.drawBrushStrokes(lines: List<PolyLine>, applyFeather: Boolean = false) =
    withStyle(brushStyle) {
      lines.forEach(this::shape)
      if (applyFeather) blurAlpha(featherPx, brushColor)
    }

  fun drawInteractive(
    sketch: BaseSketch,
    graphics: PGraphics = sketch.interactiveGraphicsLayer
  ) {
    if (!hasBoundDragListener) sketch.addMouseEventListener(mouseDragListener)

    graphics.withDraw {
      if (showMask) latestMaskDisplay?.draw(this)

      if (sketch.isMouseHovering()) drawBrushCursor(sketch.mouseLocation())

      val currentMouseDragPath = mouseDragListener.peekCurrentDrags()
      if (currentMouseDragPath.isNotEmpty() && brushType != Bucket) drawBrushStrokes(
        listOf(currentMouseDragPath),
      )
    }

    val mouseDrags = mouseDragListener.popDrags()
    if (mouseDrags.isNotEmpty()) {
      val brushMaskImage: PImage = sketch.createImage(format = RgbaOpenCV) {
        fun drawBrushAndBlur() = drawBrushStrokes(mouseDrags, applyFeather = true)

        when (brushType) {
          Brush -> drawBrushAndBlur()
          Eraser -> drawBrushAndBlur()
          Bucket -> background(brushColor.rgb)
          Gradient -> TODO()
          Line -> drawBrushStrokes(
            listOf(mouseDrags.first(), mouseDrags.last()),
            applyFeather = true,
          )
        }
      }

      getNonNullAlphaMask(sketch).overlayARGBImage(brushMaskImage)
      sketch.markDirty()
    }
  }

  private fun PGraphics.overlayARGBImage(image: PImage) {
    latestAlphaMat = withDrawToImage { overlay(image) }
      .toMat()
      .converted(RgbaOpenCV, Gray)
    updateMaskDisplay()
  }

  private fun PGraphics.drawBrushCursor(location: Point) {
    val minToShowFeatherBounds = 2.0
    val showFeatherBounds = featherPx >= minToShowFeatherBounds
    val showCursor = !(minToShowFeatherBounds..5.0).contains(feather)

    if (showFeatherBounds) withStyle(brushFeatherCursorStyle) {
      val minDiameter = size - featherPx
      val maxDiameter = size + featherPx
      circle(Circ(location, minDiameter / 2))
      circle(Circ(location, maxDiameter / 2))
    }

    if (showCursor) withStyle(brushCursorStyle) {
      circle(Circ(location, size / 2))
    }
  }

  private fun getNonNullAlphaMask(sketch: BaseSketch): PGraphics {
    val alphaMask = this.alphaMask ?: initAlphaMask(sketch)
    checkAlphaMaskSize(sketch)
    return alphaMask
  }

  private fun initAlphaMask(sketch: BaseSketch): PGraphics {
    val newAlphaMask = sketch.createGraphicsAndDraw(sketch.size, Rgb) {
      background(Color.black.rgb)

      latestAlphaMat?.asDisplayAlpha(Color.WHITE)?.draw(this)
    }

    updateMaskDisplay()

    alphaMask = newAlphaMask
    return newAlphaMask
  }

  private fun updateMaskDisplay() {
    latestMaskDisplay = latestAlphaMat
      ?.asDisplayAlpha(Color.RED)
      ?.toPImage()
  }

  private fun checkAlphaMaskSize(sketch: BaseSketch) =
    getNonNullAlphaMask(sketch).with {
      if (width != sketch.width || height != sketch.height) {
        setSize(sketch.width, sketch.height)
      }
    }
}
