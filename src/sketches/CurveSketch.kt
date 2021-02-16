package sketches

import BaseSketch
import LayerConfig
import controls.Control.Button
import controls.Control.Slider
import controls.Control.Slider2d
import controls.Control.Toggle
import controls.ControlGroupable
import controls.toControlGroups
import coordinate.BoundRect
import coordinate.Point
import java.awt.Color

open class CurveSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 9 * 72,
  sizeY: Int = 12 * 72,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.CurveSketch",
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  private val outerPaddingX: Double = sizeX * 0.02
  private val outerPaddingY: Double = sizeY * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
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

  override fun getControls(): List<ControlGroupable> = listOf(
    Button("clear") {
      points.clear()
      markDirty()
    },
    Slider("xval", defaultValue = xMidpointVal) {
      xMidpointVal = it
      markDirty()
    },
    Toggle("withHorizontalLines") {
      withHorizontalLines = it
      markDirty()
    },
    Slider("quality", defaultValue = quality) {
      quality = it
      markDirty()
    },
    Slider("numLines", range = 1.0..100.0, defaultValue = numLines.toDouble()) {
      numLines = it.toInt()
      markDirty()
    },
    Slider("lineSpacing", range = 0.0..200.0, defaultValue = lineSpacing) {
      lineSpacing = it
      markDirty()
    },
    Slider("noiseScale", range = 1.0..100.0, defaultValue = noiseScale.toDouble()) {
      noiseScale = it.toInt()
      markDirty()
    },
    Slider("moveAmount", range = 0.0..2000.0, defaultValue = moveAmount.toDouble()) {
      moveAmount = it.toInt()
      markDirty()
    },
    Slider2d("noiseOffset", rangeX = 1.0..100.0, rangeY = 1.0..10.0, defaultValue = noiseOffset) {
      noiseOffset = it
      markDirty()
    },
  ).toControlGroups()

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  private fun drawPoints() = points.forEach { point -> debugCirc(point) }

  private fun getNoiseMovement(p: Point): Point {
    val noisePoint = noiseXY((p * (noiseScale / 100.0)) + (noiseOffset * 2)) - Point(0.5, 0.5)

    return (noisePoint * moveAmount)
  }

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
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
        .forEach { linePoints -> shape(linePoints, drawBound) }
    }

    for (line in 0..numLines) {
      val x = drawBound.width * xMidpointVal + drawBound.left - (line - numLines / 2) * lineSpacing

      val linePoints = leftWithSomeOverlap.toProgression(stepsPerPixel)
        .map { Point(x, it.y) }
        .map { p -> p + getNoiseMovement(p) }

      shape(linePoints, drawBound)
    }

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(CurveSketch())
