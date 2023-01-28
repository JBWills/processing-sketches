package coordinate

import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.CounterClockwise
import coordinate.RotationDirection.EitherDirection
import coordinate.RotationDirection.WhicheverLarger

data class Cone2D(val origin: Point, val direction: Deg, val spread: Double) {
  fun contains(p: Point): Boolean {
    val segment = Segment(origin, p)
    if (segment.slope.rotation(direction + (spread / 2), dir = Clockwise) > spread) {
      return false
    }
    if (segment.slope.rotation(direction - (spread / 2), dir = CounterClockwise) > spread) {
      return false
    }

    return true
  }

  companion object {
    fun fromRays(ray: Ray, ray2: Ray, isGreaterThan180: Boolean): Cone2D {
      if (ray.origin != ray2.origin) {
        throw Exception("can't form a cone from two rays that don't share an origin: $ray, $ray2")
      }

      val clockwiseRotation = ray.slope.rotation(ray2.slope, dir = Clockwise)
      var rotationDirection = Clockwise
      if (clockwiseRotation > 180.0 && !isGreaterThan180 || clockwiseRotation < 180.0 && isGreaterThan180) {
        rotationDirection = CounterClockwise
      }

      val spread = ray.slope.rotation(
        ray2.slope,
        dir = if (isGreaterThan180) WhicheverLarger else EitherDirection,
      )

      val direction = ray.slope.rotatedTowards(Deg(spread / 2), rotationDirection)

      return Cone2D(ray.origin, direction, spread)
    }
  }
}
