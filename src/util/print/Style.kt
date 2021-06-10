package util.print

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import util.print.Alignment.Baseline
import util.print.Alignment.Center
import java.awt.Color

typealias MM = Number
typealias Px = Number
typealias Inches = Number

enum class Alignment(val alignInt: Int) {
  Center(PConstants.CENTER),
  Top(PConstants.TOP),
  Bottom(PConstants.BOTTOM),
  Baseline(PConstants.BASELINE),
}

enum class StrokeJoin(val joinInt: Int) {
  Miter(PConstants.MITER),
  Bevel(PConstants.BEVEL),
  Round(PConstants.ROUND),
  ;
}

enum class TextAlign(val textAlignX: Alignment, val textAlignY: Alignment) {
  Centered(Center, Center),
  CenterHorizontal(Center, Baseline),
  CenterVertical(Baseline, Center),
  ;

  fun apply(app: PApplet) = app.textAlign(textAlignX.alignInt, textAlignY.alignInt)
  fun apply(g: PGraphics) = g.textAlign(textAlignX.alignInt, textAlignY.alignInt)
}

data class Style(
  val weight: StrokeWeight? = null,
  val color: Color? = null,
  val join: StrokeJoin? = null,
  val fillColor: Color? = null,
  val textAlign: TextAlign? = null,
  val textSize: Number? = null,
  val noStroke: Boolean? = null,
  val noFill: Boolean? = null,
  val dpi: DPI = DPI.InkScape,
) {
  constructor(
    weight: StrokeWeight? = null,
    colorInt: Int,
    dpi: DPI = DPI.InkScape,
  ) : this(weight, Color(colorInt), null, null, null, null, null, null, dpi)

  val weightPx: Px? = weight?.toPx(dpi)

  fun apply(sketch: PApplet) {
    if (weightPx != null) sketch.strokeWeight(weightPx.toFloat())
    if (join != null) sketch.strokeJoin(join.joinInt)
    if (color != null) sketch.stroke(color.rgb, sketch.alpha(color.rgb))
    if (fillColor != null) sketch.fill(fillColor.rgb, sketch.alpha(fillColor.rgb))
    if (noStroke == true) sketch.noStroke()
    if (noFill == true) sketch.noFill()
    textAlign?.apply(sketch)
    if (textSize != null) sketch.textSize(textSize.toFloat())
  }

  fun apply(g: PGraphics) {
    if (weightPx != null) g.strokeWeight(weightPx.toFloat())
    if (join != null) g.strokeJoin(join.joinInt)
    if (color != null) g.stroke(color.rgb)
    if (fillColor != null) g.fill(fillColor.rgb)
    if (noStroke == true) g.noStroke()
    if (noFill == true) g.noFill()
    textAlign?.apply(g)
    if (textSize != null) g.textSize(textSize.toFloat())
  }

  fun applyOverrides(overrides: Style) = Style(
    overrides.weight ?: weight,
    overrides.color ?: color,
    overrides.join ?: join,
    overrides.fillColor ?: fillColor,
    overrides.textAlign ?: textAlign,
    overrides.textSize ?: textSize,
    overrides.noStroke ?: noStroke,
    overrides.noFill ?: noFill,
    overrides.dpi,
  )

  override fun toString(): String {
    return "Style(weight=$weight, color=$color)"
  }
}
