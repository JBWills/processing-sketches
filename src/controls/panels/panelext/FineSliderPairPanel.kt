package controls.panels.panelext

import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.panels.panelext.util.RefGetter
import controls.panels.panelext.util.wrapSelf
import controls.props.GenericProp
import coordinate.Point
import util.base.DoubleRange
import kotlin.reflect.KMutableProperty0

data class FineSliderPairArgs(
  val coarseRanges: Pair<DoubleRange, DoubleRange>,
  val fineRanges: Pair<DoubleRange, DoubleRange>,
  val withLockToggle: Boolean = false,
  val defaultLocked: Boolean = false,
  val style: ControlStyle? = null,
  val shouldMarkDirty: Boolean = true,
  val xName: String? = null,
  val yName: String? = null,
) {
  constructor(
    coarseRange: DoubleRange,
    fineRange: DoubleRange = 0.0..1.0,
    withLockToggle: Boolean = false,
    defaultLocked: Boolean = false,
    style: ControlStyle? = null,
    shouldMarkDirty: Boolean = true,
    xName: String? = null,
    yName: String? = null,
  ) : this(
    coarseRange to coarseRange,
    fineRange to fineRange,
    withLockToggle,
    defaultLocked,
    style,
    shouldMarkDirty,
    xName,
    yName,
  )
}

fun PanelBuilder.fineSliderPair(
  ref: KMutableProperty0<Point>,
  args: FineSliderPairArgs? = null
) = fineSliderPairPanel(ref.wrapSelf(), args)

fun PanelBuilder.fineSliderPairPanel(
  ref: RefGetter<Point>,
  args: FineSliderPairArgs? = null
): Panelable {
  val (coarseRanges, fineRanges, withLockToggle, defaultLocked, style, shouldMarkDirty, xName, yName) = args
    ?: FineSliderPairArgs(0.0..10.0, 0.0..1.0)
  return addNewPanel(style) {
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
        coarse = Point(item.x, item.y)
        ref.set(fineGetter.get() + coarse)
      }
    }

    GenericProp(ref) {
      col {
        sliderPairPanel(
          coarseGetter,
          SliderPairArgs(
            coarseRanges,
            withLockToggle = withLockToggle,
            defaultLocked = defaultLocked,
            shouldMarkDirty = shouldMarkDirty,
            xName = xName,
            yName = yName,
          ),
        )
        row {
          heightRatio = 0.75

          sliderPairPanel(
            fineGetter,
            SliderPairArgs(
              fineRanges,
              withLockToggle = withLockToggle,
              defaultLocked = defaultLocked,
              shouldMarkDirty = shouldMarkDirty,
              xName = xName,
              yName = yName,
            ),
          )
        }
      }
    }
  }
}


