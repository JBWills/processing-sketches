package appletExtensions

import BaseSketch
import SketchConfig
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import java.awt.Color

typealias Sketch = BaseSketch<out SketchConfig>

fun getTangentCornerBisector(bound: BoundRect, deg: Deg): Line {
  val isUpward = (deg.value % 180) in 0f..90f

  val leftCornerBisector = Line(bound.topLeft, Deg(-45f))
  val rightCornerBisector = Line(bound.topRight, Deg(-135f))

  return when {
    deg.isVertical() || deg.isHorizontal() || isUpward ->
      leftCornerBisector
    else -> rightCornerBisector
  }
}

// return true if segment was in bounds
fun Sketch.drawLineSegment(bound: BoundRect, line: Line): Boolean {
  val lineSegment = bound.getBoundSegment(line)
  if (lineSegment != null) {
    line(lineSegment)
    return true
  }

  return false
}

fun Sketch.drawParallelLinesInBound(
  bound: BoundRect,
  deg: Deg,
  distanceBetween: Float,
  offset: Float = 0f,
  strokeColor: Color = Color.BLACK,
) {
  fun getNormal(cornerBisector: Line, linesToDrawSlope: Deg): Line {
    val tangentLine = Line(cornerBisector.origin, linesToDrawSlope)
    val tangentNormalClockwise = tangentLine.normal(true)
    val tangentNormalCounterClockwise = tangentLine.normal(false)

    return if (cornerBisector.angleBetween(tangentNormalClockwise) <
      cornerBisector.angleBetween(tangentNormalCounterClockwise)) {
      tangentNormalClockwise
    } else {
      tangentNormalCounterClockwise
    }
  }

  fun isMovingAwayFromAllCorners(bound: BoundRect, lastPosition: Point, currentPosition: Point): Boolean {
    fun movingAwayFromCorner(corner: Point) = corner.dist(lastPosition) <= corner.dist(currentPosition)
    return movingAwayFromCorner(bound.topLeft) &&
      movingAwayFromCorner(bound.topRight) &&
      movingAwayFromCorner(bound.bottomLeft) &&
      movingAwayFromCorner(bound.bottomRight)
  }

  val cornerAndDirection = getTangentCornerBisector(bound, deg)
  val walkDirection = getNormal(cornerAndDirection, deg)

  if (isDebugMode) {
    stroke(Color.RED.rgb)
    line(walkDirection, 10, false)
    stroke(strokeColor.rgb)
  }

  var currDist = offset
  while (true) {
    val newOrigin = walkDirection.getPointAtDist(currDist)

    val wasInBound = drawLineSegment(bound, Line(newOrigin, deg))
    if (isDebugMode) {

      stroke(Color.RED.rgb)
      line(Line(newOrigin, deg), 5)
      stroke(strokeColor.rgb)
    }

    if (!wasInBound && isMovingAwayFromAllCorners(bound, walkDirection.getPointAtDist(currDist - distanceBetween), newOrigin)) {
      // if you're not currently in bounds but you were before that means you've left
      // the shape.
      break
    }

    currDist += distanceBetween
  }
}