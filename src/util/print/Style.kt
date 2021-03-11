package util.print

import BaseSketch
import appletExtensions.withStyle
import java.awt.Color

typealias MM = Number
typealias Px = Number
typealias Inches = Number

enum class StrokeWeight(val mm: MM) {
  VeryThin(0.1),
  Thin(0.5),
  Thick(1.0),
  VeryThick(2.0),
  Brush(3.0),
}

data class Style(
  val weight: StrokeWeight? = null,
  val color: Color? = null,
  val dpi: DPI = DPI.InkScape,
) {
  constructor(
    weight: StrokeWeight? = null,
    colorInt: Int,
    dpi: DPI = DPI.InkScape,
  ) : this(weight, Color(colorInt), dpi)

  val weightPx: Px? = weight?.mm?.let { dpi.toPixelsFromMm(it) }

  fun apply(sketch: BaseSketch, block: () -> Unit) = sketch.withStyle(this, block)

  fun applyOverrides(overrides: Style) = Style(
    overrides.weight ?: weight,
    overrides.color ?: color,
    overrides.dpi,
  )

  override fun toString(): String {
    return "Style(weight=$weight, color=$color)"
  }
}
