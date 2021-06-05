package controls.props.types

import BaseSketch
import appletExtensions.createGraphics
import appletExtensions.draw.circle
import appletExtensions.draw.shape
import appletExtensions.isMouseHovering
import appletExtensions.mouseLocation
import appletExtensions.withDraw
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
import geomerativefork.src.util.toRGBInt
import interfaces.listeners.MouseDragListener
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import processing.core.PGraphics
import processing.core.PImage
import processing.opengl.PGL.RGBA
import util.pointsAndLines.polyLine.PolyLine
import util.print.CustomPx
import util.print.StrokeJoin
import util.print.Style
import util.print.VeryThick
import util.withAlpha
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
    brushType: BrushType? = null
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

  val brushStyle: Style
    get() = Style(
      color = (if (brushType == Brush) Color.WHITE else Color.BLACK).withAlpha(intensity.toRGBInt()),
      weight = CustomPx(size),
      join = StrokeJoin.Round,
      noFill = true,
    )

  fun drawBrush(g: PGraphics, line: PolyLine) = g.withStyle(brushStyle) {
    shape(line)
  }

  fun drawInteractive(
    sketch: BaseSketch,
    graphics: PGraphics = sketch.interactiveGraphicsLayer
  ) = graphics.withStyle(
    Style(
      color = Color.white.withAlpha(128),
      noFill = true,
      weight = VeryThick(),
    ),
  ) {
    if (!hasBoundDragListener) {
      sketch.addMouseEventListener(mouseDragListener)
    }

    val alphaMask = getNonNullAlphaMask(sketch)

    val brush = getBrushCirc(sketch)
    graphics.withDraw {
      if (showMask) {
        latestMaskImage?.let { image(it, 0f, 0f) }
      }

      if (sketch.isMouseHovering()) circle(brush)

      val mouseDrags = mouseDragListener.peekCurrentDrags()
      if (mouseDrags.isNotEmpty()) drawBrush(this, mouseDrags)
    }

    val mouseDrags = mouseDragListener.popDrags()
    if (mouseDrags.isNotEmpty()) {
      alphaMask.withDraw {
        when (brushType) {
          Brush,
          Eraser -> mouseDrags.forEach { drawBrush(alphaMask, it) }
          Bucket -> alphaMask.background(intensity.toFloat())
        }
        latestMaskImage = alphaMask.get()
      }
    }
  }

  private fun getBrushCirc(sketch: BaseSketch) = Circ(sketch.mouseLocation(), size)

  private fun getNonNullAlphaMask(sketch: BaseSketch): PGraphics {
    val alphaMask = this.alphaMask ?: initAlphaMask(sketch)
    checkAlphaMaskSize(sketch)
    return alphaMask
  }

  private fun initAlphaMask(sketch: BaseSketch): PGraphics {
    val newAlphaMask = sketch.createGraphics(sketch.size, RGBA)
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
