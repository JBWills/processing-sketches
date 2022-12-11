package controls.props.types

import BaseSketch
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import kotlinx.serialization.Serializable
import util.base.with
import util.print.Orientation
import util.print.Paper
import util.print.StrokeWeight
import util.print.Thick


@Serializable
data class CanvasProp(
  var paper: Paper = Paper.SquareBlack,
  var boundBoxCenter: Point = Point.Half,
  var boundBoxScale: Point = Point(0.8, 0.8),
  var drawBoundRect: Boolean = true,
  var boundRectExtraWide: Boolean = true,
  var orientation: Orientation = Orientation.Landscape,
  var strokeWeight: StrokeWeight = Thick()
) : PropData<CanvasProp> {
  constructor(
    c: CanvasProp,
    paper: Paper? = null,
    boundBoxCenter: Point? = null,
    boundBoxScale: Point? = null,
    drawBoundRect: Boolean? = null,
    boundRectExtraWide: Boolean? = null,
    orientation: Orientation? = null,
    strokeWeight: StrokeWeight? = null,
  ) : this(
    paper ?: c.paper,
    boundBoxCenter ?: c.boundBoxCenter,
    boundBoxScale ?: c.boundBoxScale,
    drawBoundRect ?: c.drawBoundRect,
    boundRectExtraWide ?: c.boundRectExtraWide,
    orientation ?: c.orientation,
    strokeWeight ?: c.strokeWeight,
  )

  val pagePx: Point get() = paper.px(orientation)

  val boundRect get() = calcBoundRect()

  private fun calcBoundRect(): BoundRect =
    paper
      .toBoundRect(orientation)
      .scale(boundBoxScale, newCenter = boundBoxCenter * pagePx)

  override fun toSerializer() = serializer()

  override fun clone() = CanvasProp(this)

  fun markCanvasDirty(sketch: BaseSketch) = sketch.with {
    updateSize(pagePx)

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun bind(): List<ControlTab> = singleTab("SpiralProp") {
    row {
      heightRatio = 1.5
      dropdown(::paper) { markCanvasDirty(this) }
      dropdown(::orientation) { markCanvasDirty(this) }
      dropdown(::strokeWeight, StrokeWeight.values())
    }
    col {
      style = ControlStyle.Red
      row {
        toggle(::drawBoundRect).withHeight(0.5)
        toggle(::boundRectExtraWide).withHeight(0.5)
      }

      sliderPair(::boundBoxCenter)
      sliderPair(::boundBoxScale, SliderPairArgs(withLockToggle = true, defaultLocked = false))
    }
  }

  companion object {
    fun BaseSketch.updateCanvas(canvasProp: CanvasProp) {
      canvasProp.markCanvasDirty(this)
    }
  }
}
