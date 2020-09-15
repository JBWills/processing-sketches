package coordinate

data class Point(var x: Float, var y: Float) {
  operator fun unaryMinus() = Point(-x, -y)

  operator fun unaryPlus() = Point(+x, +y)

  operator fun plus(other: Point) = Point(x + other.x, y + other.y)

  operator fun minus(other: Point) = Point(x - other.x, y - other.y)

  override fun toString(): String {
    return "Point(x=$x, y=$y)"
  }

  companion object {
    fun add(p1: Point, p2: Point) = p1 + p2

    fun subtract(p1: Point, p2: Point) = p1 - p2
  }


}

data class PixelPoint(var x: Int, var y: Int) {
  operator fun unaryMinus() = PixelPoint(-x, -y)

  operator fun unaryPlus() = PixelPoint(+x, +y)

  operator fun plus(other: PixelPoint) = PixelPoint(x + other.x, y + other.y)

  operator fun minus(other: PixelPoint) = PixelPoint(x - other.x, y - other.y)

  companion object {
    fun add(p1: PixelPoint, p2: PixelPoint) = p1 + p2

    fun subtract(p1: PixelPoint, p2: PixelPoint) = p1 - p2
  }
}