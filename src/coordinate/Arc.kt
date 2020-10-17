package coordinate

import coordinate.RotationDirection.Clockwise
import java.lang.Exception

class Arc(var startDeg: Deg, var lengthClockwise: Float, circle: Circ) : Circ(circle.origin, circle.radius) {
  constructor(circle: Circ) : this(Deg(0), 360f, circle)

  constructor(startDeg: Deg, endDeg: Deg, circle: Circ) : this(
    startDeg,
    startDeg.rotation(endDeg, Clockwise),
    circle
  )

  constructor(
    startPoint: Point,
    endPoint: Point,
    circle: Circ,
  ) : this(
    circle.angleAtPoint(startPoint),
    circle.angleAtPoint(endPoint),
    circle
  )

  init {
    if (lengthClockwise > 360) {
      throw Exception("Can't have an arc with angle length  > 360")
    }
  }

  val angleBisector get(): Deg = startDeg + (lengthClockwise / 2)
  val pointAtBisector get(): Point = pointAtAngle(angleBisector)
  val endDeg get(): Deg = startDeg + lengthClockwise

  fun rotated(amt: Float) = Arc(startDeg + amt, lengthClockwise, Circ(origin, radius))

  fun flippedVertically() = Arc(-startDeg, -endDeg, Circ(origin, radius))
}