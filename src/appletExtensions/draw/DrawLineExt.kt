package appletExtensions.draw

import coordinate.Line
import coordinate.Point
import coordinate.Segment
import processing.core.PApplet
import processing.core.PGraphics

fun PApplet.line(p1: Point, p2: Point) = line(p1.xf, p1.yf, p2.xf, p2.yf)

fun PApplet.line(l: Segment) = line(l.p1, l.p2)

fun PApplet.line(l: Line, length: Number, centered: Boolean = true) {
  val lineExtender = (length.toDouble() / 2)

  if (centered) {
    line(l.getPointAtDist(-lineExtender), l.getPointAtDist(lineExtender))
  } else {
    line(l.origin, l.getPointAtDist(lineExtender * 2))
  }
}

fun PGraphics.line(p1: Point, p2: Point) = line(p1.xf, p1.yf, p2.xf, p2.yf)

fun PGraphics.line(l: Segment) = line(l.p1, l.p2)

fun PGraphics.line(l: Line, length: Number, centered: Boolean = true) {
  val lineExtender = (length.toDouble() / 2)

  if (centered) {
    line(l.getPointAtDist(-lineExtender), l.getPointAtDist(lineExtender))
  } else {
    line(l.origin, l.getPointAtDist(lineExtender * 2))
  }
}
