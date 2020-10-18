package util

import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Line
import coordinate.LineSegment
import coordinate.Point
import processing.core.PApplet

open class PAppletExt : PApplet() {
  fun random(low: Int, high: Int) = random(low.toFloat(), high.toFloat())
  fun random(low: Float, high: Int) = random(low, high.toFloat())
  fun random(low: Int, high: Float) = random(low.toFloat(), high)

  fun line(l: LineSegment) = line(l.p1, l.p2)
  fun line(l: Line, length: Number, centered: Boolean = true) {
    val lineExtender = (length.toFloat() / 2)

    if (centered) {
      line(l.getPointAtDist(-lineExtender), l.getPointAtDist(lineExtender))
    } else {
      line(l.origin, l.getPointAtDist(lineExtender * 2))
    }
  }

  fun line(p1: Point, p2: Point) = line(p1.x, p1.y, p2.x, p2.y)
  fun rect(r: BoundRect) = rect(r.left, r.top, r.width, r.height)

  fun circle(c: Circ) = circle(c.origin.x, c.origin.y, c.diameter)

  fun arc(a: Arc) {
    if (a.lengthClockwise >= 360f) {
      circle(a)
      return
    }

    arc(a, a.startDeg, a.endDeg)
  }

  fun arcFlipped(a: Arc) {
    if (a.lengthClockwise >= 360f) {
      circle(a)
      return
    }

    // Need to flip the arc and switch the start and end points because the arc Processing util draws counterclockwise,
    // but our arcs store start and end degrees in clockwise notation.
    val aFlipped = a.flippedVertically()
    arc(a, aFlipped.endDeg, aFlipped.startDeg)
  }

  private fun arc(c: Circ, startDeg: Deg, endDeg: Deg) {
    val endDegValue = if (endDeg.value < startDeg.value) {
      endDeg.value + 360f
    } else endDeg.value


    arc(c.origin.x, c.origin.y, c.diameter, c.diameter, startDeg.rad, endDegValue.toRadians())
  }
}