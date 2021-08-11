package controls.props.types

import arrow.core.memoize
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.dropdown
import controls.panels.panelext.slider
import controls.panels.panelext.sliderPair
import controls.props.PropData
import controls.props.types.ShapeType.Ellipse
import controls.props.types.ShapeType.Rectangle
import coordinate.BoundRect
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import geomerativefork.src.RPath
import interfaces.shape.Maskable
import kotlinx.serialization.Serializable
import util.ZeroToOne
import util.geomutil.contains
import util.geomutil.diff
import util.geomutil.ellipse
import util.geomutil.intersection
import util.geomutil.rotated
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

  fun getRPath(bound: BoundRect): RPath = getRPathMemo(bound, type, size, center, rotation)

  fun contains(p: Point, sketchBound: BoundRect) = getRPath(sketchBound).contains(p)

  // Not accurate with rotation, sorry!
  fun roughBounds(bound: BoundRect) = centeredRect(bound.pointAt(center.x, center.y), size)

  fun asMaskable(boundRect: BoundRect): Maskable {
    val centerPoint = boundRect.pointAt(center.x, center.y)
    return when (type) {
      Ellipse -> Circ(centerPoint, size.x)
      Rectangle -> centeredRect(centerPoint, size.x, size.y)
    }
  }

  fun intersection(boundRect: BoundRect, polyLine: PolyLine) =
    polyLine.intersection(asMaskable(boundRect))

  fun diff(boundRect: BoundRect, polyLine: PolyLine) =
    polyLine.diff(asMaskable(boundRect))

  override fun toSerializer() = serializer()

  override fun clone() = ShapeProp(this)

  override fun bind(): List<ControlTab> = singleTab("ShapeProp") {
    dropdown(::type)
    sliderPair(::center, ZeroToOne to ZeroToOne)
    sliderPair(::size, 0.0..2000.0, withLockToggle = true, defaultLocked = true)
    slider(::rotation)
  }
}

private val getRPathMemo = {
    bound: BoundRect,
    type: ShapeType,
    size: Point,
    centerRatio: Point,
    rotation: Deg,
  ->
  val centerPoint = bound.pointAt(centerRatio.x, centerRatio.y)

  val rShape =
    if (type == Ellipse) ellipse(centerPoint, size)
    else centeredRect(centerPoint, size).toRShape()

  rShape
    .also { it.polygonize() }
    .rotated(rotation, centerPoint)
    .paths
    .first()
}.memoize()
