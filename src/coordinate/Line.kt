package coordinate

import util.xf
import util.yf
import java.awt.geom.Line2D

class LineSegment(
  var p1: Point,
  var p2: Point,
) {
  val length: Float
    get() = p1.dist(p2)

  fun toLine2d(): Line2D = Line2D.Float(p1.x, p1.y, p2.x, p2.y)
}

class Line(
  var crossesThrough: Point,
  var slope: Deg,
) {

  fun intersect(other: Line): Point? {
    var this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  fun intersect(other: LineSegment): Point? {
    var this2d = toLine2d()
    val that2d = other.toLine2d()
    if (!this2d.intersectsLine(that2d)) return null

    return getIntersectPoint(this2d, that2d)
  }

  fun intersectsY(y: Float): Point? = intersect(Line(Point(0f, y), Deg(0)))
  fun intersectsX(x: Float): Point? = intersect(Line(Point(x, 0f), Deg(90)))

  fun toLine2d(): Line2D {
    val extender = slope.unitVector * 100000
    val startPoint = crossesThrough + extender
    val endPoint = crossesThrough - extender
    return Line2D.Float(startPoint.x, startPoint.y, endPoint.x, endPoint.y)
  }

  private fun getIntersectPoint(l1: Line2D, l2: Line2D): Point? {
    val denominator = (
      (l1.p2.xf - l1.p1.xf) * (l2.p2.yf - l2.p1.yf) -
        (l1.p2.yf - l1.p1.yf) * (l2.p2.xf - l2.p1.xf))

    if (denominator == 0f) return null

    val numerator = (
      (l1.p1.yf - l2.p1.yf) * (l2.p2.xf - l2.p1.xf) -
        (l1.p1.xf - l2.p1.xf) * (l2.p2.yf - l2.p1.yf))
    val r = numerator / denominator

    val intersectX = l1.p1.xf + r * (l1.p2.xf - l1.p1.xf)
    val intersectY = l1.p1.yf + r * (l1.p2.yf - l1.p1.yf)
    return Point(intersectX, intersectY)
  }

  override fun toString(): String {
    return "Line(crossesThrough=$crossesThrough, slope=$slope)"
  }
}