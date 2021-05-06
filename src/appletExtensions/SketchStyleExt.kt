package appletExtensions

import processing.core.PApplet
import util.print.Style
import java.awt.Color

fun PApplet.stroke(c: Color) = stroke(c.rgb)

fun PApplet.withStroke(c: Int, block: () -> Unit) {
  pushStyle()
  stroke(c)
  block()
  popStyle()
}

fun PApplet.withFill(c: Int, block: () -> Unit) {
  pushStyle()
  fill(c)
  block()
  popStyle()
}

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStroke(c: Color, block: () -> Unit) =
  withStroke(c.rgb, block)

fun PApplet.withFill(c: Color, block: () -> Unit) =
  withFill(c.rgb, block)

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
