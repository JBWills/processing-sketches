package controls.props.types

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import coordinate.PointSpiral
import kotlinx.serialization.Serializable
import util.base.ZeroToOne
import util.polylines.PolyLine
import util.polylines.polyLine.normalizeDistances

@Serializable
data class SpiralProp(
  var sizeStart: Point = Point(0, 0),
  var sizeEnd: Point = Point(100, 100),
  var origin: Point = Point.Half,
  var numRotations: Int = 10,
  var numRotationsFine: Double = 0.0,
  var rotationStart: Double = 0.0,
  var step: Double = 1.0,
) : PropData<SpiralProp> {
  constructor(
    s: SpiralProp,
    sizeStart: Point? = null,
    sizeEnd: Point? = null,
    origin: Point? = null,
    numRotations: Int? = null,
    numRotationsFine: Double? = null,
    rotationStart: Double? = null,
    step: Double? = null,
  ) : this(
    sizeStart ?: s.sizeStart,
    sizeEnd ?: s.sizeEnd,
    origin ?: s.origin,
    numRotations ?: s.numRotations,
    numRotationsFine ?: s.numRotationsFine,
    rotationStart ?: s.rotationStart,
    step ?: s.step,
  )

  override fun toSerializer() = serializer()

  override fun clone() = SpiralProp(this)

  override fun bind(): List<ControlTab> = singleTab("SpiralProp") {
    slider2D(::origin, range = -Point.Half..Point(1.5, 1.5))
    sliderPair(
      ::sizeStart,
      0.0..1000.0,
      defaultLocked = true,
      withLockToggle = true,
    )
    sliderPair(
      ::sizeEnd,
      0.0..1000.0,
      defaultLocked = true,
      withLockToggle = true,
    )

    row {
      slider(::numRotations, 1..100)
      slider(::numRotationsFine, 0.0..1.0).withWidth(0.5)
    }

    slider(::rotationStart, ZeroToOne)

    slider(::step, 0.01..10.0)
  }

  fun spiral(
    drawBounds: BoundRect,
    mapPoint: (t: Double, percent: Double, deg: Deg, p: Point) -> Point,
  ): PolyLine = PointSpiral(
    drawBounds.pointAt(origin),
    sizeRange = (sizeStart..sizeEnd),
    rotationsRange = rotationStart..(rotationStart + numRotations + numRotationsFine),
  ) { t, percent, deg, point ->
    mapPoint(t, percent, deg, point)
  }.walk(step / 100).normalizeDistances(step..(step))
}
