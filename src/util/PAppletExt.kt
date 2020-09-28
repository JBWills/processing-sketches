package util

import coordinate.BoundRect
import coordinate.LineSegment
import coordinate.Point
import processing.core.PApplet

open class PAppletExt : PApplet() {
  fun random(low: Int, high: Int) = random(low.toFloat(), high.toFloat())
  fun random(low: Float, high: Int) = random(low, high.toFloat())
  fun random(low: Int, high: Float) = random(low.toFloat(), high)

  fun line(l: LineSegment) = line(l.p1, l.p2)
  fun line(p1: Point, p2: Point) = line(p1.x, p1.y, p2.x, p2.y)
  fun rect(r: BoundRect) = rect(r.top, r.left, r.width, r.height)
}