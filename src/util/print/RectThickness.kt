package util.print

import coordinate.BoundRect
import util.base.step

private const val ExpandStepPX = 0.2

@Suppress("unused")
enum class RectThickness(val expandAmountPx: Double) {
  Regular(0.0),
  Thick(0.2),
  ExtraThick(0.4),
  ExtraExtraThick(1.0),
  ;


  fun getBoundRectsToDraw(bounds: BoundRect): List<BoundRect> =
    (-expandAmountPx..expandAmountPx).step(ExpandStepPX)
      .map { expandAmount -> bounds.expand(expandAmount) }
}
