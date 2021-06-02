package controls.props.types

import BaseSketch
import appletExtensions.isMouseHovering
import appletExtensions.mouseLocation
import appletExtensions.withStroke
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
import coordinate.Mat2D
import coordinate.Mat2D.Companion.createCircle
import kotlinx.serialization.Serializable
import processing.core.PConstants.ALPHA
import processing.core.PImage
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

  val mask = Mat2D(1000, 1000)

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

  override fun clone() = BrushProp(this)

  override fun bind(): List<ControlTab> = singleTab("SpiralProp") {
    row {
      col {
        button("Clear") { mask.clear(); markDirty() }
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

  fun applyStroke(circ: Circ) = when (brushType) {
    Brush -> mask.addCentered(circ.origin, createCircle(circ, intensity, feather))
    Eraser -> mask.subtractCentered(circ.origin, createCircle(circ, intensity, feather))
    Bucket -> mask.setAll(intensity)
  }

  fun getBrushCirc(sketch: BaseSketch) = Circ(sketch.mouseLocation(), size)

  fun drawInteractive(sketch: BaseSketch) = sketch.withStroke(Color(255, 255, 255, 128)) {
    if (showMask) {
      val image = PImage(mask.width, mask.height, ALPHA).apply { mask(mask.toIntMatrix(255)) }
      sketch.image(image, 0f, 0f)
    }

    val brush = getBrushCirc(sketch)

    if (sketch.isMouseHovering()) {
      sketch.circle(brush)
    }

    if (sketch.mousePressed) {
      applyStroke(brush)
    }
  }
}
