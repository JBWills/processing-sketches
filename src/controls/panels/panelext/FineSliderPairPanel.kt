package controls.panels.panelext

import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.wrapSelf
import controls.props.GenericProp
import coordinate.Point
import util.base.DoubleRange
import util.numbers.floorInt
import util.tuple.and
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.fineSliderPair(
  ref: KMutableProperty0<Point>,
  coarseRange: DoubleRange = 0.0..1.0,
  fineRange: DoubleRange = 0.0..1.0,
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = fineSliderPairPanel(
  ref.wrapSelf(),
  coarseRange to coarseRange,
  fineRange to fineRange,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
)

fun PanelBuilder.fineSliderPair(
  ref: KMutableProperty0<Point>,
  coarseRanges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  fineRanges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
) = fineSliderPairPanel(
  ref.wrapSelf(),
  coarseRanges,
  fineRanges,
  withLockToggle,
  defaultLocked,
  style,
  shouldMarkDirty,
)

private fun PanelBuilder.fineSliderPairPanel(
  ref: RefGetter<Point>,
  coarseRanges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  fineRanges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
  withLockToggle: Boolean = false,
  defaultLocked: Boolean = false,
  style: ControlStyle? = null,
  shouldMarkDirty: Boolean = true,
  xName: String? = null,
  yName: String? = null,
) = addNewPanel(style) {
  var coarseGetter: RefGetter<Point>? = null

  val fineGetter: RefGetter<Point> = object : RefGetter<Point> {
    var fine: Point = Point.Zero
    override val name: String = "${ref.name}_fine"
    override fun get(): Point = fine
    override fun set(item: Point) {
      fine = item
      ref.set(fine + (coarseGetter?.get() ?: Point.Zero))
    }
  }

  coarseGetter = object : RefGetter<Point> {
    var coarse: Point = ref.get()
    override val name: String = "${ref.name}_coarse"
    override fun get(): Point = coarse
    override fun set(item: Point) {
      coarse = Point(item.x.floorInt(), item.y.floorInt())
      ref.set(fineGetter.get() + coarse)
    }
  }

  GenericProp(ref) {
    col {
      sliderPairPanel(
        coarseGetter,
        coarseRanges,
        withLockToggle = withLockToggle,
        defaultLocked = defaultLocked,
        shouldMarkDirty = shouldMarkDirty,
        xName = xName,
        yName = yName,
      )
      row {
        heightRatio = 0.75

        sliderPairPanel(
          fineGetter,
          fineRanges,
          withLockToggle = withLockToggle,
          defaultLocked = defaultLocked,
          shouldMarkDirty = shouldMarkDirty,
          xName = xName,
          yName = yName,
        )
      }
    }
  }
}


