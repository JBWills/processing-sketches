package sketches

import BaseSketch
import FastNoiseLite.NoiseType
import FastNoiseLite.NoiseType.Perlin
import LayerConfig
import appletExtensions.draw.rect
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import controls.panels.panelext.button
import controls.panels.panelext.dropdown
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.toggle
import coordinate.BoundRect
import coordinate.Point
import java.awt.Color

open class WarpSketch(
  backgroundColor: Color = Color.WHITE,
  size: Point = Point(9 * 72, 12 * 72),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "WarpSketch",
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
  private var withHorizontalLines: Boolean = true
  private var withVerticalLines: Boolean = true
  private var lineSpacing: Double = 30.0
  private var noiseScale: Int = 15
  private var moveAmountX: Int = 1
  private var moveAmountY: Int = 1
  private var noiseOffset: Point = Point(0, 0)
  private var quality: Double = 0.5
  private var seed: Int = 1000
  private var noiseType: NoiseType = Perlin

  override fun getControls(): Panelable = col {
    button("clear") {
      points.clear()
      markDirty()
    }

    slider(::xMidpointVal)

    dropdown(::noiseType)

    row {
      toggle(::withVerticalLines)
      toggle(::withHorizontalLines)
    }

    slider(::quality)

    row {
      slider(::numLines, range = 1..100)
      slider(::lineSpacing, range = 0.001..200.0)
    }

    slider(::noiseScale, range = 1..100)

    row {
      slider(::moveAmountX, range = 0..2000)
      slider(::moveAmountY, range = 0..2000)
    }

    slider(::seed, range = 0..2000)

    row {
      heightRatio = 3
      slider2D(::noiseOffset, Point.One..Point(1000, 1000))
    }
  }

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  private fun drawPoints() = points.forEach { point -> debugCirc(point) }

  private fun getNoiseMovement(p: Point): Point {
    val noisePoint = noiseXY((p * (noiseScale / 100.0)) + (noiseOffset * 2))
    return (noisePoint * Point(moveAmountX, moveAmountY))
  }

  override suspend fun SequenceScope<Unit>.drawOnce(layer: Int, layerConfig: LayerConfig) {
    NOISE.SetSeed(seed)
    NOISE.SetNoiseType(noiseType)
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

    val leftWithSomeOverlap = drawBound.leftSegment.expand(moveAmountY * 2)

    if (withHorizontalLines) {
      leftWithSomeOverlap.toProgression(lineSpacing)
        .map { it.y }
        .map { y ->
          (Point(xMax, y)..Point(xMin, y) step stepsPerPixel).map { p -> p + getNoiseMovement(p) }
        }
        .forEach { linePoints -> shape(linePoints, drawBound) }
    }

    if (withVerticalLines) {
      for (line in 0..numLines) {
        val x =
          drawBound.width * xMidpointVal + drawBound.left - (line - numLines / 2) * lineSpacing

        val linePoints = leftWithSomeOverlap.toProgression(stepsPerPixel)
          .map { Point(x, it.y) }
          .map { p -> p + getNoiseMovement(p) }

        shape(linePoints, drawBound)
      }
    }

    rect(drawBound)
  }
}

fun main() = WarpSketch().run()
