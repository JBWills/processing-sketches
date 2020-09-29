package sketches

import BaseSketch
import SketchConfig
import controls.Control
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import java.awt.Color

class GradientLinesConfig : SketchConfig() {
}

class GradientLinesSketch(
  private var lineDegrees: Int = 0,
  private val isDebugMode: Boolean = false
) : BaseSketch<GradientLinesConfig>(
  backgroundColor = Color.BLACK,
  svgBaseFileName = "sketches.CircleSketch",
  sketchConfig = null,
  sizeX = 1000,
  sizeY = 1500
) {

  private val outerPaddingX: Float = sizeX * 0.2f
  private val outerPaddingY: Float = sizeY * 0.4f
  private var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf(
    Control.Slider(
      text = "Line angle (degrees)",
      range = 0f to 360f,
      handleChange =
      {
        lineDegrees = it.toInt()
        markDirty()
      }
    )
  )

  override fun getRandomizedConfig() = GradientLinesConfig()

  override fun drawOnce(config: GradientLinesConfig) {
    fun getTangentCornerBisector(bound: BoundRect, deg: Deg): Line {
      val isUpward = (deg.value % 180) in 0..90

      val leftCornerBisector = Line(bound.topLeft, Deg(-45))
      val rightCornerBisector = Line(bound.topRight, Deg(-135))

      return when {
        deg.isVertical() || deg.isHorizontal() || isUpward ->
          leftCornerBisector
        else -> rightCornerBisector
      }
    }

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

    // return true if segment was in bounds
    fun drawLineSegment(bound: BoundRect, line: Line): Boolean {
      val lineSegment = bound.getBoundSegment(line)
      if (lineSegment != null) {
        line(lineSegment)
        return true
      }

      return false
    }

    fun isMovingAwayFromAllCorners(bound: BoundRect, lastPosition: Point, currentPosition: Point): Boolean {
      fun movingAwayFromCorner(corner: Point) = corner.dist(lastPosition) <= corner.dist(currentPosition)
      return movingAwayFromCorner(bound.topLeft) &&
        movingAwayFromCorner(bound.topRight) &&
        movingAwayFromCorner(bound.bottomLeft) &&
        movingAwayFromCorner(bound.bottomRight)
    }

    fun walkPoints(bound: BoundRect, deg: Deg, distanceBetween: Float, offset: Float = 0f) {
      val cornerAndDirection = getTangentCornerBisector(bound, deg)
      val walkDirection = getNormal(cornerAndDirection, deg)

      if (isDebugMode) {
        stroke(Color.RED.rgb)
        line(walkDirection, 10, false)
        stroke(Color.WHITE.rgb)
      }

      var currDist = offset
      while (true) {
        val newOrigin = walkDirection.getPointAtDist(currDist)

        val wasInBound = drawLineSegment(bound, Line(newOrigin, deg))
        if (isDebugMode) {
          stroke(Color.RED.rgb)
          line(Line(newOrigin, deg), 5)
          stroke(Color.WHITE.rgb)
        }

        if (!wasInBound && isMovingAwayFromAllCorners(bound, walkDirection.getPointAtDist(currDist - distanceBetween), newOrigin)) {
          // if you're not currently in bounds but you were before that means you've left
          // the shape.
          break
        }

        currDist += distanceBetween
      }
    }

    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()

    val segmentHeight = drawBound.height / 7f

    fun bounds(segIndexFromTop: Int, numSegs: Int = 1) = BoundRect(
      drawBound.topLeft + Point(0f, segIndexFromTop * segmentHeight),
      segmentHeight * numSegs,
      drawBound.width
    )

    rect(drawBound)
    walkPoints(
      bounds(0, 1),
      Deg(lineDegrees),
      distanceBetween = 2f
    )

    walkPoints(
      bounds(1, 1),
      Deg(lineDegrees),
      distanceBetween = 4f
    )

    walkPoints(
      bounds(2, 1),
      Deg(lineDegrees),
      distanceBetween = 8f
    )

    walkPoints(
      bounds(3, 1),
      Deg(lineDegrees),
      distanceBetween = 16f
    )

    walkPoints(
      bounds(4, 1),
      Deg(lineDegrees),
      distanceBetween = 32f
    )

    walkPoints(
      bounds(5, 1),
      Deg(lineDegrees),
      distanceBetween = 64f
    )

    walkPoints(
      bounds(6, 1),
      Deg(lineDegrees),
      distanceBetween = 128f
    )
  }
}

fun main() = BaseSketch.run(GradientLinesSketch())