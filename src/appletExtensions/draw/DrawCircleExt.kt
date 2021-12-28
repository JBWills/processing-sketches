package appletExtensions.draw

import appletExtensions.clipInsideRect
import appletExtensions.withStyle
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import processing.core.PApplet
import processing.core.PGraphics
import util.numbers.toRadians
import util.print.CustomPx
import util.print.Style

fun PGraphics.circle(c: Point, r: Double) = circle(c.xf, c.yf, r.toFloat() * 2)
fun PGraphics.circle(c: Circ) = circle(c.origin.xf, c.origin.yf, c.diameter.toFloat())

fun PApplet.point(p: Point) {
  // We may need to override the strokeweight if it's <1, because the java renderer doesn't show
  // points with weight < 1
  val strokeWeightOverrides: Style? =
    if (graphics.strokeWeight < 1) Style(weight = CustomPx(1.0)) else null
  withStyle(strokeWeightOverrides) {
    point(p.xf, p.yf)
  }
}

fun PApplet.circle(c: Point, r: Double) = circle(c.xf, c.yf, r.toFloat() * 2)
fun PApplet.circle(c: Circ) = circle(c.origin.xf, c.origin.yf, c.diameter.toFloat())

fun PApplet.circle(x: Number, y: Number, r: Number) =
  ellipse(x.toFloat(), y.toFloat(), r.toFloat(), r.toFloat())

fun PApplet.arc(a: Arc) {
  if (a.lengthClockwise >= 360.0) {
    circle(a)
    return
  }

  arc(a, a.startDeg, a.endDeg)
}

fun PApplet.arcFlipped(a: Arc) {
  if (a.lengthClockwise >= 360.0) {
    circle(a)
    return
  }

  // Need to flip the arc and switch the start and end points because the arc Processing util draws counterclockwise,
  // but our arcs store start and end degrees in clockwise notation.
  val aFlipped = a.flippedVertically()
  arc(a, aFlipped.endDeg, aFlipped.startDeg)
}

private fun PApplet.arc(c: Circ, startDeg: Deg, endDeg: Deg) {
  val endDegValue = if (endDeg.value < startDeg.value) {
    endDeg.value + 360.0
  } else endDeg.value


  arc(
    c.origin.xf, c.origin.yf, c.diameter.toFloat(), c.diameter.toFloat(),
    startDeg.rad.toFloat(), endDegValue.toRadians().toFloat(),
  )
}

fun PApplet.arcs(arcs: Iterable<Arc>) = arcs.forEach { a -> arc(a) }

fun PApplet.boundArc(arc: Arc, bound: BoundRect) = arcs(arc.clipInsideRect(bound))
