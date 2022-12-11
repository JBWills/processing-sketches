package controls.props.types

import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.panels.ControlStyle
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.FineSliderPairArgs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.fineSliderPair
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import kotlinx.serialization.Serializable
import util.polylines.PolyLine
import kotlin.math.PI

@Serializable
data class HarmonicProp(
  var step: Double = 0.5,
  var dist: Double = 2 * PI,
  var size: Point = Point(100, 100),
  var amplitude: Point = Point(100, 100),
  var center: Point = Point.Half,
  var frequency: Point = Point.One,
  var delta: Point = Point.Zero,
  var dampening: Point = Point(0.1, 0.1),
  var controlsLabel: String = "Harmonic",
) : PropData<HarmonicProp> {
  constructor(
    h: HarmonicProp,
    step: Double? = null,
    dist: Double? = null,
    size: Point? = null,
    center: Point? = null,
    frequency: Point? = null,
    delta: Point? = null,
    dampening: Point? = null,
  ) : this(
    step = step ?: h.step,
    dist = dist ?: h.dist,
    size = size ?: h.size,
    center = center ?: h.center,
    frequency = frequency ?: h.frequency,
    delta = delta ?: h.delta,
    dampening = dampening ?: h.dampening,
  )

  override fun toSerializer() = serializer()

  override fun clone() = HarmonicProp(this)

  override fun bind(): List<ControlTab> = singleTab(controlsLabel) {
    row {
      heightRatio = 0.5
      slider(::step, 0.01..1.0)
      slider(::dist, 0.0..100_000.0)
    }

    row {
      heightRatio = 3.0
      slider2D(::center, Slider2DArgs(-1..2))
      col {
        widthRatio = 0.2
        button("reset") {
          this@HarmonicProp.center = Point.Half
          markDirty()
        }
      }
    }
    sliderPair(
      ::size,
      SliderPairArgs(
        1.0..2_000.0,
        defaultLocked = true,
        withLockToggle = true,
      ),
    )

    row {
      style = ControlStyle.Black
      fineSliderPair(
        ::frequency,
        FineSliderPairArgs(
          coarseRange = 0.0..15.0,
          fineRange = 0.0..1.0,
          defaultLocked = true,
          withLockToggle = true,
        ),
      )
    }
    row {
      style = ControlStyle.Green
      fineSliderPair(
        ::delta,
        FineSliderPairArgs(
          coarseRange = 0.0..15.0,
          fineRange = 0.0..1.0,
          defaultLocked = true,
          withLockToggle = true,
        ),
      )
    }
    row {
      style = ControlStyle.Blue
      fineSliderPair(
        ::dampening,
        FineSliderPairArgs(
          coarseRange = 0.0001..0.5,
          fineRange = 0.0..0.05,
          defaultLocked = true,
          withLockToggle = true,
        ),
      )
    }
  }

  private fun valueAt(t: Double): Point =
    Point.sin(frequency * t + delta) *
      size *
      Point.exp(-dampening * t)

  fun harmonic(drawBounds: BoundRect, vararg otherHarmonics: HarmonicProp): PolyLine {
    val centerPoint = drawBounds.pointAt(center)

    val result = mutableListOf<Point>()
    var t = 0.0
    while (t < dist) {
      result.add(
        centerPoint + Point.sum(
          listOf(this, *otherHarmonics).map { it.valueAt(t) },
        ),
      )
      t += step
    }

    return result
  }
}
