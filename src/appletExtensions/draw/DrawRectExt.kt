package appletExtensions.draw

import coordinate.BoundRect
import processing.core.PApplet

fun PApplet.rect(left: Number, top: Number, width: Number, height: Number) = rect(
  left.toFloat(),
  top.toFloat(),
  width.toFloat(),
  height.toFloat(),
)

fun PApplet.rect(r: BoundRect) = rect(r.left, r.top, r.width, r.height)
