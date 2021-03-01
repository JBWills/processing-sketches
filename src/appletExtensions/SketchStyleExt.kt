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

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStroke(c: Color, block: () -> Unit) =
  withStroke(c.rgb, block)

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun PApplet.withStyle(s: Style, block: () -> Unit) {
  pushStyle()
  if (s.weightPx != null) strokeWeight(s.weightPx.toFloat())
  if (s.color != null) stroke(s.color.rgb)

  block()
  popStyle()
}
