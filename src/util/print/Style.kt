package util.print

import processing.core.PApplet
import processing.core.PConstants
import util.print.Alignment.Baseline
import util.print.Alignment.Center
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

enum class Alignment(val alignInt: Int) {
  Center(PConstants.CENTER),
  Top(PConstants.TOP),
  Bottom(PConstants.BOTTOM),
  Baseline(PConstants.BASELINE),
}

enum class TextAlign(val textAlignX: Alignment, val textAlignY: Alignment) {
  Centered(Center, Center),
  CenterHorizontal(Center, Baseline),
  CenterVertical(Baseline, Center),
  ;

  fun apply(app: PApplet) {
    app.textAlign(textAlignX.alignInt, textAlignY.alignInt)
  }
}

data class Style(
  val weight: StrokeWeight? = null,
  val color: Color? = null,
  val fillColor: Color? = null,
  val textAlign: TextAlign? = null,
  val textSize: Number? = null,
  val dpi: DPI = DPI.InkScape,
) {
  constructor(
    weight: StrokeWeight? = null,
    colorInt: Int,
    dpi: DPI = DPI.InkScape,
  ) : this(weight, Color(colorInt), null, null, null, dpi)

  val weightPx: Px? = weight?.mm?.let { dpi.toPixelsFromMm(it) }

  fun apply(sketch: PApplet) {
    if (weightPx != null) sketch.strokeWeight(weightPx.toFloat())
    if (color != null) sketch.stroke(color.rgb)
    if (fillColor != null) sketch.fill(fillColor.rgb)
    textAlign?.apply(sketch)
    if (textSize != null) sketch.textSize(textSize.toFloat())
  }

  fun applyOverrides(overrides: Style) = Style(
    overrides.weight ?: weight,
    overrides.color ?: color,
    overrides.fillColor ?: fillColor,
    overrides.textAlign ?: textAlign,
    overrides.textSize ?: textSize,
    overrides.dpi,
  )

  override fun toString(): String {
    return "Style(weight=$weight, color=$color)"
  }
}
