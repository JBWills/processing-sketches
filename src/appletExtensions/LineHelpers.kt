package appletExtensions

import BaseSketch
import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import java.awt.Color

typealias Sketch = BaseSketch

private fun getTangentCornerBisector(bound: BoundRect, deg: Deg): Line {
  val isUpward = (deg.value % 180) in 0.0..90.0

  val leftCornerBisector = Line(bound.topLeft, Deg(-45))
  val rightCornerBisector = Line(bound.topRight, Deg(-135))

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

private fun getNormal(cornerBisector: Line, linesToDrawSlope: Deg): Line {
  val tangentLine = Line(cornerBisector.origin, linesToDrawSlope)
  val tangentNormalClockwise = tangentLine.normal(true)
  val tangentNormalCounterClockwise = tangentLine.normal(false)

  return if (cornerBisector.angleBetween(tangentNormalClockwise) <
    cornerBisector.angleBetween(tangentNormalCounterClockwise)
  ) {
    tangentNormalClockwise
  } else {
    tangentNormalCounterClockwise
  }
}

private fun isMovingAwayFromAllCorners(
  bound: BoundRect, lastPosition: Point, currentPosition: Point,
): Boolean {
  fun movingAwayFromCorner(corner: Point) =
    corner.dist(lastPosition) <= corner.dist(currentPosition)
  return movingAwayFromCorner(bound.topLeft) &&
    movingAwayFromCorner(bound.topRight) &&
    movingAwayFromCorner(bound.bottomLeft) &&
    movingAwayFromCorner(bound.bottomRight)
}

private val getParallelLinesInBoundBase = { bound: BoundRect,
                                            deg: Deg,
                                            distanceBetween: Number,
                                            offset: Number ->
  val cornerAndDirection = getTangentCornerBisector(bound, deg)
  val walkDirection = getNormal(cornerAndDirection, deg)
  var currDist = offset.toDouble()
  val segments = mutableListOf<Segment>()
  while (true) {
    val newOrigin = walkDirection.getPointAtDist(currDist)

    val nullableSegment = bound.getBoundSegment(Line(newOrigin, deg))
    nullableSegment?.let { segment ->
      segments.add(segment)
    }
    val wasInBound = nullableSegment != null

    if (
      !wasInBound &&
      isMovingAwayFromAllCorners(
        bound,
        walkDirection.getPointAtDist(
          currDist - distanceBetween.toDouble(),
        ),
        newOrigin,
      )
    ) {
      // if you're not currently in bounds but you were before that means you've left
      // the shape.
      break
    }

    currDist += distanceBetween.toDouble()
  }

  segments
}

val getParallelLinesInBoundBaseMemoized = { bound: BoundRect,
                                            deg: Deg,
                                            distanceBetween: Number,
                                            offset: Number ->
  getParallelLinesInBoundBase(bound, deg, distanceBetween, offset)
}.memoize()

fun getParallelLinesInBound(
  bound: BoundRect,
  deg: Deg,
  distanceBetween: Number,
  offset: Number = 0.0,
): List<Segment> = getParallelLinesInBoundBase(bound, deg, distanceBetween, offset)

fun getParallelLinesInBoundMemo(
  bound: BoundRect,
  deg: Deg,
  distanceBetween: Number,
  offset: Number = 0.0,
): List<Segment> = getParallelLinesInBoundBaseMemoized(bound, deg, distanceBetween, offset)

fun Sketch.drawParallelLinesInBound(
  bound: BoundRect,
  deg: Deg,
  distanceBetween: Number,
  offset: Number = 0.0,
  strokeColor: Color = Color.BLACK,
) {
  val cornerAndDirection = getTangentCornerBisector(bound, deg)
  val walkDirection = getNormal(cornerAndDirection, deg)

  if (isDebugMode) {
    stroke(Color.RED.rgb)
    line(walkDirection, 10, false)
    stroke(strokeColor.rgb)
  }

  var currDist = offset.toDouble()
  while (true) {
    val newOrigin = walkDirection.getPointAtDist(currDist)

    val wasInBound = drawLineSegment(bound, Line(newOrigin, deg))
    if (isDebugMode) {

      stroke(Color.RED.rgb)
      line(Line(newOrigin, deg), 5)
      stroke(strokeColor.rgb)
    }

    if (!wasInBound && isMovingAwayFromAllCorners(
        bound,
        walkDirection.getPointAtDist(currDist - distanceBetween.toDouble()), newOrigin,
      )
    ) {
      // if you're not currently in bounds but you were before that means you've left
      // the shape.
      break
    }

    currDist += distanceBetween.toDouble()
  }
}
