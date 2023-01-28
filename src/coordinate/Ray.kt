package coordinate

data class Ray(val origin: Point, val slope: Deg) {

  fun intersection(line: Line): Point? {
    val p = line.intersection(toLine()) ?: return null

    if (!hasPoint(p)) {
      return null
    }

    return p
  }

  fun hasPoint(p: Point): Boolean = getSlope(origin, p) == slope

  fun toLine() = Line(origin, slope)
}
