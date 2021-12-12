package appletExtensions

import processing.core.PApplet
import processing.core.PGraphics
import util.base.withAlpha
import util.print.Style
import java.awt.Color

fun PApplet.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())
fun PGraphics.stroke(c: Color) = stroke(c.rgb, c.alpha.toFloat())

inline fun <R> PApplet.withStroke(c: Int, alpha: Int? = null, block: PApplet.() -> R): R =
  withStyle(Style(color = Color(c).withAlpha(alpha)), block)

inline fun <R> PGraphics.withStroke(c: Int, alpha: Int? = null, block: PGraphics.() -> R): R =
  withStyle(Style(color = Color(c).withAlpha(alpha)), block)

inline fun <R> PApplet.withFill(c: Int, alpha: Int = 255, block: PApplet.() -> R): R =
  withStyle(Style(fillColor = Color(c).withAlpha(alpha)), block)

inline fun <R> PGraphics.withFill(c: Int, alpha: Int = 255, block: PGraphics.() -> R): R =
  withStyle(Style(fillColor = Color(c).withAlpha(alpha)), block)

inline fun <R> PApplet.withStrokeIf(predicate: Boolean, c: Color, block: PApplet.() -> R): R =
  if (predicate) {
    withStroke(c, block)
  } else {
    block()
  }

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
inline fun <R> PApplet.withStroke(c: Color?, block: PApplet.() -> R): R =
  if (c != null) withStroke(c.rgb, c.alpha, block) else block()

inline fun <R> PApplet.withFill(c: Color?, block: PApplet.() -> R): R =
  if (c != null) withFill(c.rgb, c.alpha, block) else block()

inline fun <R> PApplet.withFillAndStroke(
  strokeColor: Color?,
  fillColor: Color? = strokeColor,
  block: PApplet.() -> R
): R = withStroke(strokeColor) { withFill(fillColor, block) }

inline fun <R> PGraphics.withStroke(c: Color, block: PGraphics.() -> R): R =
  withStroke(c.rgb, c.alpha, block)

inline fun <R> PGraphics.applyWithStroke(c: Color, block: PGraphics.() -> R): R =
  withStroke(c.rgb, c.alpha) { this.block() }

inline fun <R> PGraphics.withFill(c: Color, block: PGraphics.() -> R): R =
  withFill(c.rgb, c.alpha, block)

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
inline fun <R> PApplet.withStrokeNonNull(c: Color?, block: PApplet.() -> R): R =
  if (c != null) withStroke(c, block) else block()

inline fun <R> PApplet.withFillNonNull(fill: Color?, block: PApplet.() -> R): R =
  if (fill != null) withFill(fill, block) else block()

/**
 * Run the block after applying stroke color, then switch back to
 * last styling.
 */
inline fun <R> PApplet.withStyle(s: Style?, block: PApplet.() -> R): R {
  val recorderFromStart: PGraphics? = recorder
  val graphicsFromStart: PGraphics = g
  s?.let {
    pushStyle()
    s.apply(this)
  }
  val result = block()
  s?.let {
    // Sometimes the recorder switches during the block() call. In that case we don't want to pop
    // the new recorder's style (that could break things).
    recorderFromStart?.popStyle()
    graphicsFromStart.popStyle()
  }
  return result
}

inline fun <R> PGraphics.withStyle(s: Style?, block: PGraphics.() -> R): R {
  s?.let {
    pushStyle()
    s.apply(this)
  }
  val result = block()
  s?.let { popStyle() }
  return result
}
