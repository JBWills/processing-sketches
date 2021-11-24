package sketches.legacy

import BaseSketch
import LayerConfig
import appletExtensions.draw.line
import appletExtensions.draw.rect
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import controls.controlsealedclasses.Slider.Companion.slider
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import util.numbers.pow
import util.numbers.times
import java.awt.Color

class GradientLinesOldSketch(var lineDegrees: Int = 0) : BaseSketch(
  backgroundColor = Color.BLACK,
  svgBaseFileName = "svgs.CircleSketch",
) {

  val outerPaddingX: Double = size.x * 0.2
  val outerPaddingY: Double = size.y * 0.2
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    size.x - 2 * outerPaddingX,
    size.y - 2 * outerPaddingY,
  )

  override fun getControls(): Panelable = col {
    slider(::lineDegrees, 0..360)
  }

  override suspend fun SequenceScope<Unit>.drawOnce(
    layer: Int,
    layerConfig: LayerConfig,
  ) {
    fun getPointsLinesShouldCrossThrough(
      bound: BoundRect, deg: Deg, numLines: Int,
    ): Iterable<Point> {
      val isUpward = deg.value in 90f..180f || deg.value in 270f..360f
      val start = if (isUpward) bound.topLeft else bound.topRight
      val end = if (isUpward) bound.bottomRight else bound.bottomLeft

      return start.lerp(end, numLines)
    }

    fun drawLines(
      bound: BoundRect,
      numLines: Int,
      direction: Deg,
      addFinalLine: Boolean = false,
    ) {
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
      drawBound.width,
      segmentHeight * numSegs,
    )

    rect(drawBound)
    3.times {
      drawLines(
        bound = bounds(it + 1),
        numLines = 4.pow(it),
        direction = Deg.HORIZONTAL,
        addFinalLine = true,
      )
    }

    drawLines(
      bound = bounds(5, 4),
      numLines = 4.pow(3),
      direction = Deg.HORIZONTAL,
      addFinalLine = true,
    )

    drawLines(
      bound = bounds(5),
      numLines = 5,
      direction = Deg.VERTICAL,
      addFinalLine = true,
    )
    drawLines(
      bound = bounds(6),
      numLines = 10,
      direction = Deg.VERTICAL,
      addFinalLine = true,
    )
    drawLines(
      bound = bounds(7),
      numLines = 20,
      direction = Deg.VERTICAL,
      addFinalLine = true,
    )
    drawLines(
      bound = bounds(8),
      numLines = 30,
      direction = Deg.VERTICAL,
      addFinalLine = true,
    )
    drawLines(
      bound = bounds(9),
      numLines = 40,
      direction = Deg.VERTICAL,
      addFinalLine = true,
    )

  }
}

fun main() = GradientLinesOldSketch().run()
