package util.print

import processing.core.PGraphics
import processing.core.PStyle

fun PGraphics.applyStyle(style: PStyle) {
  fill(style.fillColor)

  strokeWeight(style.strokeWeight)
  strokeCap(style.strokeCap)
  strokeJoin(style.strokeJoin)
  stroke(style.strokeColor)


  textAlign(style.textAlign, style.textAlignY)
  textSize(style.textSize)
  textLeading(style.textLeading)
  style.textFont?.let { textFont(it) }
  textLeading(style.textLeading)
  textMode(style.textMode)

  colorMode(style.colorMode, style.colorModeX, style.colorModeY, style.colorModeZ, style.colorModeA)

  imageMode(style.imageMode)
  rectMode(style.rectMode)
  ellipseMode(style.ellipseMode)
  shapeMode(style.shapeMode)

  if (!style.fill) noFill()
  if (!style.stroke) noStroke()
  if (!tint) noTint()
}
