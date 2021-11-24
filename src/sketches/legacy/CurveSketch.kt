package sketches.legacy

import BaseSketch
import LayerConfig
import appletExtensions.draw.rect
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Point
import util.tuple.and
import java.awt.Color

open class CurveSketch(
  backgroundColor: Color = Color.WHITE,
  size: Point = Point(9 * 72, 12 * 72),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.CurveSketch",
  size = size,
) {

  private val outerPaddingX: Double = size.x * 0.02
  private val outerPaddingY: Double = size.y * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    size.x - 2 * outerPaddingX,
    size.y - 2 * outerPaddingY,
  )

  private val points: MutableList<Point> = mutableListOf()
  private var xMidpointVal: Double = 0.5
  private var numLines: Int = 10
  private var withHorizontalLines: Boolean = false
  private var lineSpacing: Double = 30.0
  private var noiseScale: Int = 1
  private var moveAmount: Int = 1
  private var noiseOffset: Point = Point(0, 0)
  private var quality: Double = 0.5

  override fun getControls(): Panelable = col {
    button("clear") {
      points.clear()
      markDirty()
    }
    slider(::xMidpointVal)
    toggle(::withHorizontalLines)
    slider(::quality)
    slider(::numLines, range = 1..100)
    slider(::lineSpacing, range = 0.0..200.0)
    slider(::noiseScale, range = 1..100)
    slider(::moveAmount, range = 0..2000)
    slider2D(::noiseOffset, 1.0..100.0 and 1.0..10.0)
  }

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  private fun drawPoints() = points.forEach { point -> debugCirc(point) }

  private fun getNoiseMovement(p: Point): Point {
    val noisePoint = noiseXY((p * (noiseScale / 100.0)) + (noiseOffset * 2)) - Point(0.5, 0.5)

    return (noisePoint * moveAmount)
  }

  override suspend fun SequenceScope<Unit>.drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()

    drawPoints()

    val stepsPerPixel = when {
      quality < 0.1 -> 100
      quality < 0.2 -> 50
      quality < 0.3 -> 25
      quality < 0.4 -> 10
      quality < 0.5 -> 5
      quality < 0.6 -> 2
      quality < 0.7 -> 1
      quality < 0.8 -> 0.5
      quality < 0.9 -> 0.25
      else -> 0.1
    }.toDouble()

    val xMin = drawBound.width * xMidpointVal + drawBound.left - (0 - numLines / 2) * lineSpacing
    val xMax =
      drawBound.width * xMidpointVal + drawBound.left - (numLines - numLines / 2) * lineSpacing

    val leftWithSomeOverlap = drawBound.leftSegment.expand(moveAmount * 2)

    if (withHorizontalLines) {
      leftWithSomeOverlap.toProgression(lineSpacing)
        .map { it.y }
        .map { y ->
          (Point(xMax, y)..Point(xMin, y) step stepsPerPixel).map { p -> p + getNoiseMovement(p) }
        }
        .draw(drawBound)
    }

    for (line in 0..numLines) {
      val x = drawBound.width * xMidpointVal + drawBound.left - (line - numLines / 2) * lineSpacing

      val linePoints = leftWithSomeOverlap.toProgression(stepsPerPixel)
        .map { Point(x, it.y) }
        .map { p -> p + getNoiseMovement(p) }

      linePoints.draw(drawBound)
    }

    rect(drawBound)
  }
}

fun main() = CurveSketch().run()
