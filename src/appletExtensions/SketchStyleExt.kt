package appletExtensions

import processing.core.PApplet
import util.print.Style
import java.awt.Color

fun PApplet.stroke(c: Color) = stroke(c.rgb)

fun PApplet.withStroke(c: Int, alpha: Int = 255, block: () -> Unit) {
  pushStyle()
  stroke(c, alpha / 255f)
  block()
  popStyle()
}

fun PApplet.withFill(c: Int, alpha: Int = 255, block: () -> Unit) {
  pushStyle()
  fill(c, alpha / 255f)
  block()
  popStyle()
}

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStroke(c: Color, block: () -> Unit) =
  withStroke(c.rgb, c.alpha, block)

fun PApplet.withFill(c: Color, block: () -> Unit) =
  withFill(c.rgb, c.alpha, block)

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStrokeNonNull(c: Color?, block: () -> Unit) =
  if (c != null) withStroke(c, block) else block()

fun PApplet.withFillNonNull(fill: Color?, block: () -> Unit) =
  if (fill != null) withFill(fill, block) else block()

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStyle(s: Style, block: () -> Unit) {
  pushStyle()
  s.apply(this)

  block()
  popStyle()
}
