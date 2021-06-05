package appletExtensions

import processing.core.PApplet
import processing.core.PGraphics
import util.print.Style
import java.awt.Color

fun PApplet.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())
fun PGraphics.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())

fun PApplet.withStroke(c: Int, alpha: Int? = null, block: () -> Unit) {
  pushStyle()
  stroke(c, alpha?.toFloat() ?: 255f)
  block()
  popStyle()
}

fun PGraphics.withStroke(c: Int, alpha: Int? = null, block: () -> Unit) {
  pushStyle()
  stroke(c, alpha?.toFloat() ?: 255f)
  block()
  popStyle()
}

fun PApplet.withFill(c: Int, alpha: Int = 255, block: () -> Unit) {
  pushStyle()
  fill(c, alpha.toFloat())
  block()
  popStyle()
}

fun PGraphics.withFill(c: Int, alpha: Int = 255, block: () -> Unit) {
  pushStyle()
  fill(c, alpha.toFloat())
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

fun PGraphics.withStroke(c: Color, block: () -> Unit) =
  withStroke(c.rgb, c.alpha, block)

fun PGraphics.applyWithStroke(c: Color, block: PGraphics.() -> Unit) =
  withStroke(c.rgb, c.alpha) { this.block() }

fun PGraphics.withFill(c: Color, block: () -> Unit) =
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

fun PGraphics.withStyle(s: Style, block: PGraphics.() -> Unit) {
  pushStyle()
  s.apply(this)
  block()
  popStyle()
}
