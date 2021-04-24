package controls.props.types

import arrow.core.memoize
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import controls.props.types.ShapeType.Ellipse
import coordinate.BoundRect
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Deg
import coordinate.Point
import geomerativefork.src.RPath
import kotlinx.serialization.Serializable
import util.ZeroToOne
import util.geomutil.contains
import util.geomutil.ellipse
import util.geomutil.rotated

enum class ShapeType {
  Ellipse,
  Rectangle,
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

  override fun toSerializer() = serializer()

  override fun clone() = ShapeProp(this)

  override fun bind(): List<ControlTab> = singleTab("ShapeProp") {
    dropdownList(::type)
    sliderPair(::center, ZeroToOne to ZeroToOne)
    sliderPair(::size, 0.0..2000.0, withLockToggle = true, defaultLocked = true)
    degreeSlider(::rotation)
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
