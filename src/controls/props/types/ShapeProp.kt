package controls.props.types

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.sliderPair
import controls.props.PropData
import controls.props.types.ShapeType.Ellipse
import controls.props.types.ShapeType.Rectangle
import coordinate.BoundRect
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import kotlinx.serialization.Serializable
import util.base.ZeroToOne
import util.polylines.PolyLine

enum class ShapeType {
  Ellipse,
  Rectangle,
  ;
}

@Serializable
data class ShapeProp(
  var type: ShapeType = Ellipse,
  var size: Point = Point(200, 200),
  var center: Point = Point(0.5, 0.5),
  var rotation: Deg = Deg(0),
) : PropData<ShapeProp> {
  constructor(
    s: ShapeProp,
    type: ShapeType? = null,
    size: Point? = null,
    center: Point? = null,
    rotation: Deg? = null,
  ) : this(
    type ?: s.type,
    size ?: s.size,
    center ?: s.center,
    rotation ?: s.rotation,
  )

  private fun getCirc(bound: BoundRect): Circ =
    Circ(bound.pointAt(center.x, center.y), size.x)

  private fun getRect(bound: BoundRect): BoundRect =
    centeredRect(bound.pointAt(center.x, center.y), size)

  fun getPolyLine(bound: BoundRect): PolyLine {
    val centerPoint = bound.pointAt(center.x, center.y)

    return if (type == Ellipse) Circ(centerPoint, size.x).toPolyLine()
    else centeredRect(centerPoint, size).toPolyLine()
  }

  fun contains(bound: BoundRect, p: Point) = when (type) {
    Ellipse -> getCirc(bound).contains(p)
    Rectangle -> getRect(bound).contains(p)
  }

  // Not accurate with rotation, sorry!
  fun roughBounds(bound: BoundRect) =
    centeredRect(bound.pointAt(center.x, center.y), size)

  override fun toSerializer() = serializer()

  override fun clone() = ShapeProp(this)

  override fun bind(): List<ControlTab> = singleTab("ShapeProp") {
    dropdown(::type)
    sliderPair(::center, SliderPairArgs(range = ZeroToOne))
    sliderPair(::size, SliderPairArgs(0.0..2000.0, withLockToggle = true, defaultLocked = true))
    slider(::rotation)
  }
}
