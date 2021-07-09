package coordinate

import interfaces.shape.Scalable
import interfaces.shape.Translateable

data class PixelPoint(val x: Int, val y: Int) : Scalable<PixelPoint>, Translateable<PixelPoint> {
  operator fun unaryMinus() = PixelPoint(-x, -y)

  operator fun unaryPlus() = PixelPoint(+x, +y)

  operator fun plus(other: PixelPoint) = PixelPoint(x + other.x, y + other.y)

  operator fun minus(other: PixelPoint) = PixelPoint(x - other.x, y - other.y)

  fun toPoint() = Point(x, y)

  override fun scaled(scale: Point, anchor: Point): PixelPoint =
    toPoint().scaled(scale, anchor).toPixelPoint()

  override fun translated(translate: Point): PixelPoint =
    toPoint().translated(translate).toPixelPoint()

  companion object {
    fun add(p1: PixelPoint, p2: PixelPoint) = p1 + p2

    fun subtract(p1: PixelPoint, p2: PixelPoint) = p1 - p2
  }
}
