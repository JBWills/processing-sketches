package controls.props

import BaseSketch
import arrow.core.memoize
import controls.degProp
import controls.doublePairProp
import controls.enumProp
import controls.panels.ControlTab
import controls.panels.ControlTab.Companion.singleTab
import controls.props.ShapeType.Ellipse
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import geomerativefork.src.RShape.Companion.createEllipse
import geomerativefork.src.RShape.Companion.createRectangle
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.ZeroToOne

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

  fun getRPath(bounds: BoundRect) = _getRPath(bounds)

  @Transient
  private val _getRPath = { bounds: BoundRect ->
    val center = bounds.pointAt(center.x, center.y).toRPoint()

    if (type == Ellipse) {
      createEllipse(center, size.toRPoint())
    } else {
      createRectangle(center, size.x, size.y)
    }.paths.first()
  }.memoize()

  override fun toSerializer() = serializer()

  override fun clone() = ShapeProp(this)

  override fun BaseSketch.bind(): List<ControlTab> = singleTab(
    "ShapeProp",
    enumProp(::type),
    doublePairProp(::center, ZeroToOne to ZeroToOne),
    doublePairProp(::size, 0.0..2000.0, withLockToggle = true, defaultLocked = true),
    degProp(::rotation),
  )
}
