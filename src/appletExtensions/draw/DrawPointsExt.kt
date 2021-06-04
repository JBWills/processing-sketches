package appletExtensions.draw

import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import processing.core.PApplet

fun PApplet.drawPoint(p: Point, radius: Number = 2) {
  if (radius.toDouble() > 0) circle(Circ(p, radius))
}

fun PApplet.drawPoints(points: Iterable<Point>, radius: Number = 2) =
  points.forEach { drawPoint(it, radius) }

fun PApplet.drawSquare(point: Point, size: Number = 2, rotation: Deg = Deg(0)) {
  if (size.toDouble() > 0)
    BoundRect.centeredRect(point, Point(size, size))
      .toRPath()
      .apply { rotate(rotation.rad.toFloat()) }.draw()
}

