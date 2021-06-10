package controls.props.types

import BaseSketch
import appletExtensions.copyAndEditAsMat
import appletExtensions.createGraphicsAndDraw
import appletExtensions.createImage
import appletExtensions.draw.circle
import appletExtensions.draw.shape
import appletExtensions.isMouseHovering
import appletExtensions.mouseLocation
import appletExtensions.withDraw
import appletExtensions.withDrawToImage
import appletExtensions.withStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.button
import controls.panels.panelext.dropdown
import controls.panels.panelext.slider
import controls.panels.panelext.toggle
import controls.props.PropData
import controls.props.types.BrushType.Brush
import controls.props.types.BrushType.Bucket
import controls.props.types.BrushType.Eraser
import coordinate.Circ
import coordinate.Point
import interfaces.listeners.MouseDragListener
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import processing.core.PGraphics
import processing.core.PImage
import util.image.ImageFormat
import util.image.ImageFormat.RGB
import util.image.blurAlpha
import util.image.luminanceToAlpha
import util.image.overlay
import util.lerp
import util.pointsAndLines.polyLine.PolyLine
import util.print.CustomPx
import util.print.StrokeJoin
import util.print.Style
import util.withAlpha
import util.withAlphaDouble
import java.awt.Color

enum class BrushType {
  Brush,
  Eraser,
  Bucket,
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

  @Contextual
  var alphaMask: PGraphics? = null

  @Contextual
  var latestMaskImage: PImage? = null

  @Contextual
  val mouseDragListener = MouseDragListener()

  @Contextual
  var hasBoundDragListener = false

  constructor(
    s: BrushProp,
    size: Double? = null,
    feather: Double? = null,
    intensity: Double? = null,
    showMask: Boolean? = null,
    brushType: BrushType? = null,
  ) : this(
    size ?: s.size,
    feather ?: s.feather,
    intensity ?: s.intensity,
    showMask ?: s.showMask,
    brushType ?: s.brushType,
  )

  override fun toSerializer() = serializer()

  override fun clone() = copy()

  override fun bind(): List<ControlTab> = singleTab("SpiralProp") {
    row {
      col {
        button("Clear") { alphaMask?.clear(); markDirty() }
        toggle(::showMask)
        dropdown(::brushType)
      }

      col {
        slider(::size, 0.0..200.0)
        slider(::feather, 0.0..1.0)
        slider(::intensity, 0.0..1.0)
      }
    }
  }

  private val featherPx: Double get() = feather * size

  private val brushColor: Color
    get() = when (brushType) {
      Brush -> Color.WHITE.withAlphaDouble(intensity)
      Eraser -> Color.BLACK.withAlphaDouble(intensity)
      Bucket -> listOf(Color.BLACK, Color.WHITE).lerp(intensity)
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

    val alphaMask = getNonNullAlphaMask(sketch)

    graphics.withDraw {
      if (showMask) {
        latestMaskImage
          ?.copyAndEditAsMat { luminanceToAlpha(Color.RED.withAlphaDouble(0.5)) }
          ?.also { image(it, 0f, 0f) }
      }

      if (sketch.isMouseHovering()) drawBrushCursor(sketch.mouseLocation())

      val currentMouseDragPath = mouseDragListener.peekCurrentDrags()
      if (currentMouseDragPath.isNotEmpty() && brushType != Bucket) drawBrushStrokes(
        listOf(currentMouseDragPath),
      )
    }

    val mouseDrags = mouseDragListener.popDrags()
    if (mouseDrags.isNotEmpty()) {
      val brushMaskImage: PImage = sketch.createImage(format = ImageFormat.ARGB) {
        fun drawBrushAndBlur() = drawBrushStrokes(mouseDrags, applyFeather = true)

        when (brushType) {
          Brush -> drawBrushAndBlur()
          Eraser -> drawBrushAndBlur()
          Bucket -> background(brushColor.rgb)
        }
      }

      latestMaskImage = alphaMask.withDrawToImage { overlay(brushMaskImage) }
    }
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
    val newAlphaMask = sketch.createGraphicsAndDraw(sketch.size, RGB) {
      background(Color.black.rgb)
    }

    alphaMask = newAlphaMask
    return newAlphaMask
  }

  private fun checkAlphaMaskSize(sketch: BaseSketch) {
    val alphaMask = alphaMask ?: initAlphaMask(sketch)
    if (alphaMask.width != sketch.width || alphaMask.height != sketch.height) {
      alphaMask.setSize(sketch.width, sketch.height)
    }
  }
}
