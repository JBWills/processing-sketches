package appletExtensions.draw

import appletExtensions.withStroke
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import processing.core.PApplet
import java.awt.Color

fun PApplet.drawPoint(p: Point, radius: Number = 2, color: Color? = null) {
  withStroke(color) {
    if (radius.toDouble() > 1) circle(Circ(p, radius))
    else if (radius.toDouble() > 0) point(p)
  }
}

fun PApplet.drawPoints(points: Iterable<Point>, radius: Number = 2) =
  points.forEach { drawPoint(it, radius) }

fun PApplet.drawSquare(point: Point, size: Number = 2, rotation: Deg = Deg(0)) {
  if (size.toDouble() > 0)
    shape(BoundRect.centeredRect(point, Point(size, size)).toPolyLine())
}

