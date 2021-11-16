package controls.props.types

import BaseSketch
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.dropdown
import controls.panels.panelext.listDropdown
import controls.panels.panelext.sliderPair
import controls.panels.panelext.toggle
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
  var orientation: Orientation = Orientation.Landscape,
  var strokeWeight: StrokeWeight = Thick()
) : PropData<CanvasProp> {
  constructor(
    c: CanvasProp,
    paper: Paper? = null,
    boundBoxCenter: Point? = null,
    boundBoxScale: Point? = null,
    drawBoundRect: Boolean? = null,
    orientation: Orientation? = null,
    strokeWeight: StrokeWeight? = null,
  ) : this(
    paper ?: c.paper,
    boundBoxCenter ?: c.boundBoxCenter,
    boundBoxScale ?: c.boundBoxScale,
    drawBoundRect ?: c.drawBoundRect,
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
      listDropdown(::strokeWeight, StrokeWeight.values())
    }
    col {
      style = ControlStyle.Red
      toggle(::drawBoundRect).withHeight(0.5)
      sliderPair(::boundBoxCenter)
      sliderPair(::boundBoxScale, withLockToggle = true, defaultLocked = false)
    }
  }

  companion object {
    fun BaseSketch.updateCanvas(canvasProp: CanvasProp) {
      canvasProp.markCanvasDirty(this)
    }
  }
}
