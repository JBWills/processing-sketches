package appletExtensions

import processing.core.PApplet
import processing.core.PGraphics
import util.print.Style
import java.awt.Color

fun PApplet.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())
fun PGraphics.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())

fun <R> PApplet.withStroke(c: Int, alpha: Int? = null, block: PApplet.() -> R): R {
  pushStyle()
  stroke(c, alpha?.toFloat() ?: 255f)
  val result = block()
  popStyle()
  return result
}

fun <R> PGraphics.withStroke(c: Int, alpha: Int? = null, block: PGraphics.() -> R): R {
  pushStyle()
  stroke(c, alpha?.toFloat() ?: 255f)
  val result = block()
  popStyle()
  return result
}

fun <R> PApplet.withFill(c: Int, alpha: Int = 255, block: PApplet.() -> R): R {
  pushStyle()
  fill(c, alpha.toFloat())
  val result = block()
  popStyle()
  return result
}

fun <R> PGraphics.withFill(c: Int, alpha: Int = 255, block: PGraphics.() -> R): R {
  pushStyle()
  fill(c, alpha.toFloat())
  val result = block()
  popStyle()
  return result
}

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun <R> PApplet.withStroke(c: Color, block: PApplet.() -> R): R =
  withStroke(c.rgb, c.alpha, block)

fun <R> PApplet.withFill(c: Color, block: PApplet.() -> R): R =
  withFill(c.rgb, c.alpha, block)

fun <R> PGraphics.withStroke(c: Color, block: PGraphics.() -> R): R =
  withStroke(c.rgb, c.alpha, block)

fun <R> PGraphics.applyWithStroke(c: Color, block: PGraphics.() -> R): R =
  withStroke(c.rgb, c.alpha) { this.block() }

fun <R> PGraphics.withFill(c: Color, block: PGraphics.() -> R): R =
  withFill(c.rgb, c.alpha, block)

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun <R> PApplet.withStrokeNonNull(c: Color?, block: PApplet.() -> R): R =
  if (c != null) withStroke(c, block) else block()

fun <R> PApplet.withFillNonNull(fill: Color?, block: PApplet.() -> R): R =
  if (fill != null) withFill(fill, block) else block()

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
fun <R> PApplet.withStyle(s: Style?, block: PApplet.() -> R): R {
  s?.let {
    pushStyle()
    s.apply(this)
  }
  val result = block()
  s?.let { popStyle() }
  return result
}

fun <R> PGraphics.withStyle(s: Style?, block: PGraphics.() -> R): R {
  s?.let {
    pushStyle()
    s.apply(this)
  }
  val result = block()
  s?.let { popStyle() }
  return result
}
