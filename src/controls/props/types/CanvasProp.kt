package controls.props.types

import BaseSketch
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import kotlinx.serialization.Serializable
import util.print.Orientation
import util.print.Paper


@Serializable
data class CanvasProp(
  var paper: Paper = Paper.SquareBlack,
  var boundBoxCenter: Point = Point.Half,
  var boundBoxScale: Point = Point(0.8, 0.8),
  var drawBoundRect: Boolean = true,
  var orientation: Orientation = Orientation.Landscape,
) : PropData<CanvasProp> {
  constructor(
    c: CanvasProp,
    paper: Paper? = null,
    boundBoxCenter: Point? = null,
    boundBoxScale: Point? = null,
    drawBoundRect: Boolean? = null,
    orientation: Orientation? = null,
  ) : this(
    paper ?: c.paper,
    boundBoxCenter ?: c.boundBoxCenter,
    boundBoxScale ?: c.boundBoxScale,
    drawBoundRect ?: c.drawBoundRect,
    orientation ?: c.orientation,
  )

  val pagePx: Point = paper.px(orientation)

  val boundRect get() = calcBoundRect()

  private fun calcBoundRect(): BoundRect =
    paper
      .toBoundRect(orientation)
      .scale(boundBoxScale, newCenter = boundBoxCenter * pagePx)

  override fun toSerializer() = serializer()

  override fun clone() = CanvasProp(this)

  private fun BaseSketch.markCanvasDirty() {
    updateSize(pagePx)

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun bind(): List<ControlTab> = singleTab("SpiralProp") {
    dropdownList(::paper) { markCanvasDirty() }
    dropdownList(::orientation) { markCanvasDirty() }
    col {
      style = ControlStyle.Red
      toggle(::drawBoundRect)
      sliderPair(::boundBoxCenter)
      sliderPair(::boundBoxScale, withLockToggle = true, defaultLocked = false)
    }
  }
}
