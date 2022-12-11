package controls.panels.panelext

import controls.controlsealedclasses.Slider
import controls.controlsealedclasses.Toggle
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.pointWrapped
import controls.panels.panelext.util.wrapSelf
import controls.panels.panelext.util.wrapped
import controls.props.GenericProp
import coordinate.Point
import util.base.DoubleRange
import kotlin.reflect.KMutableProperty0

data class SliderPairArgs(
  val range1: DoubleRange,
  val range2: DoubleRange,
  val withLockToggle: Boolean = false,
  val defaultLocked: Boolean = false,
  val style: ControlStyle? = null,
  val shouldMarkDirty: Boolean = true,
  val xName: String? = null,
  val yName: String? = null,
) {
  constructor(
    ranges: Pair<DoubleRange, DoubleRange>,
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    xName: String? = null,
    yName: String? = null,
  ) :
    this(
      ranges.first,
      ranges.second,
      withLockToggle,
      defaultLocked,
      style,
      shouldMarkDirty,
      xName,
      yName,
    )

  constructor(
    range: DoubleRange = 0.0..1.0,
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    xName: String? = null,
    yName: String? = null,
  ) :
    this(
      range,
      range,
      withLockToggle,
      defaultLocked,
      style,
      shouldMarkDirty,
      xName,
      yName,
    )
}

@JvmName("sliderPairDoubles")
fun PanelBuilder.sliderPair(
  ref1: KMutableProperty0<Double>,
  ref2: KMutableProperty0<Double>,
  args: SliderPairArgs? = null,
) = sliderPairPanel(pointWrapped(ref1, ref2), args)

@JvmName("sliderPairDoubles")
fun PanelBuilder.sliderPair(
  ref1: RefGetter<Double>,
  ref2: RefGetter<Double>,
  args: SliderPairArgs? = null,
) = sliderPairPanel((ref1 to ref2).wrapped({ Point(first, second) }, { x to y }), args)

@JvmName("sliderPairDoubleRange")
fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<DoubleRange>,
  args: SliderPairArgs? = null,
) = sliderPairPanel(ref.pointWrapped(), args)

@JvmName("sliderPairPoint")
fun PanelBuilder.sliderPair(ref: KMutableProperty0<Point>, args: SliderPairArgs? = null) =
  sliderPairPanel(ref.wrapSelf(), args)

@JvmName("sliderPairDoublePair")
fun PanelBuilder.sliderPair(
  ref: KMutableProperty0<Pair<Double, Double>>,
  args: SliderPairArgs? = null,
) = sliderPairPanel(ref.pointWrapped(), args)

internal fun PanelBuilder.sliderPairPanel(
  ref: RefGetter<Point>,
  args: SliderPairArgs? = null,
): Panelable {
  val (range1, range2, withLockToggle, defaultLocked, style, shouldMarkDirty, xName, yName) = args
    ?: SliderPairArgs()
  return addNewPanel(style) {
    GenericProp(ref) {
      var locked: Boolean = defaultLocked && ref.get().x == ref.get().y
      var ctrlY: Slider? = null
      val ctrlX = Slider(
        xName ?: "${ref.name} X",
        range = range1,
        getter = { ref.get().x },
        setter = {
          if (locked) {
            ref.set(Point(it, it))
            ctrlY?.refValue = it.toFloat()
          } else {
            ref.set(ref.get().withX(it))
          }
        },
      ) { markDirtyIf(shouldMarkDirty) }

      ctrlY = Slider(
        yName ?: "${ref.name} Y",
        range = range2,
        getter = { ref.get().y },
        setter = {
          if (locked) {
            ref.set(Point(it, it))
            ctrlX.refValue = it.toFloat()
          } else {
            ref.set(ref.get().withY(it))
          }
        },
      ) { markDirtyIf(shouldMarkDirty) }

      val ctrlToggle = Toggle(
        text = "Lock ${ref.name}",
        defaultValue = locked,
      ) {
        locked = it
        markDirtyIf(shouldMarkDirty)
      }.withWidth(0.5)

      row(name) {
        +ctrlX
        +ctrlY
        if (withLockToggle) +ctrlToggle
      }
    }
  }
}

