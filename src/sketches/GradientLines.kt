package sketches

import BaseSketch
import SketchConfig
import controls.Control
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.LineSegment
import coordinate.Point
import util.pEach
import util.pow
import util.times
import java.awt.Color
import java.util.Vector

class GradientLinesConfig() : SketchConfig() {
}

class GradientLinesSketch(var lineDegrees: Int = 0) : BaseSketch<CircleConfig>(
  backgroundColor = Color.BLACK,
  svgBaseFileName = "sketches.CircleSketch",
  sketchConfig = null,
) {

  val outerPaddingX: Float = SIZE_X * 0.2f
  val outerPaddingY: Float = SIZE_Y * 0.2f
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    SIZE_Y - 2 * outerPaddingY,
    SIZE_X - 2 * outerPaddingX
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

  override fun getRandomizedConfig() = CircleConfig(
    distanceBetween = random(49, 50),
    circleLimit = random(1, 20).toInt(),
    originPointsX = random(5, 10).toInt(),
    originPointsY = random(5, 50).toInt()
  )

  override fun drawOnce(config: CircleConfig) {
    fun getPointsLinesShouldCrossThrough(bound: BoundRect, deg: Deg, numLines: Int): Iterable<Point> {
      val isUpward = deg.value in 90..180 || deg.value in 270..360
      val start = if (isUpward) bound.topLeft else bound.topRight
      val end = if (isUpward) bound.bottomRight else bound.bottomLeft

      return start.lerp(end, numLines)
    }

    fun drawLines(bound: BoundRect, numLines: Int, direction: Deg, addFinalLine: Boolean = false) {
      getPointsLinesShouldCrossThrough(bound, direction, numLines)
        .filterIndexed { index, _ -> addFinalLine || index != numLines - 1 }
        .mapNotNull { point -> bound.getBoundSegment(Line(point, direction)) }
        .forEach { segment -> line(segment) }
    }

    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()

    val segmentHeight = drawBound.height / 9f

    fun bounds(segIndexFromTop: Int, numSegs: Int = 1) = BoundRect(
      drawBound.topLeft + Point(0f, segIndexFromTop * segmentHeight),
      segmentHeight * numSegs,
      drawBound.width
    )

    rect(drawBound)
    3.times {
      drawLines(
        bound = bounds(it + 1),
        numLines = 4.pow(it),
        direction = Deg.HORIZONTAL,
        addFinalLine = true
      )
    }

    drawLines(
      bound = bounds(5, 4),
      numLines = 4.pow(3),
      direction = Deg.HORIZONTAL,
      addFinalLine = true
    )

    drawLines(
      bound = bounds(5),
      numLines = 5,
      direction = Deg.VERTICAL,
      addFinalLine = true
    )
    drawLines(
      bound = bounds(6),
      numLines = 10,
      direction = Deg.VERTICAL,
      addFinalLine = true
    )
    drawLines(
      bound = bounds(7),
      numLines = 20,
      direction = Deg.VERTICAL,
      addFinalLine = true
    )
    drawLines(
      bound = bounds(8),
      numLines = 30,
      direction = Deg.VERTICAL,
      addFinalLine = true
    )
    drawLines(
      bound = bounds(9),
      numLines = 40,
      direction = Deg.VERTICAL,
      addFinalLine = true
    )

  }
}

fun main() = BaseSketch.run(GradientLinesSketch())