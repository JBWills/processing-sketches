package sketches

import BaseSketch
import SketchConfig
import controls.Control.Button
import controls.ControlGroup
import coordinate.BoundRect
import coordinate.Point
import fastnoise.FastNoise.NoiseType
import fastnoise.FastNoise.NoiseType.PerlinFractal
import util.property2DSlider
import util.propertyEnumDropdown
import util.propertySlider
import util.propertyToggle
import java.awt.Color

class WarpConfig : SketchConfig()

open class WarpSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 9 * 72,
  sizeY: Int = 12 * 72,
) : BaseSketch<WarpConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "WarpSketch",
  sketchConfig = null,
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
  private var withHorizontalLines: Boolean = true
  private var withVerticalLines: Boolean = true
  private var lineSpacing: Double = 30.0
  private var noiseScale: Int = 15
  private var moveAmountX: Int = 1
  private var moveAmountY: Int = 1
  private var noiseOffset: Point = Point(0, 0)
  private var quality: Double = 0.5
  private var seed: Int = 1000
  private var noiseType: NoiseType = PerlinFractal

  override fun getControls() = listOf(
    ControlGroup(
      Button("clear") {
        points.clear()
        markDirty()
      }
    ),
    ControlGroup(
      propertySlider(::xMidpointVal)
    ),
    ControlGroup(propertyEnumDropdown(::noiseType), heightRatio = 5),
    ControlGroup(
      propertyToggle(::withVerticalLines),
      propertyToggle(::withHorizontalLines)
    ),
    ControlGroup(propertySlider(::quality)),
    ControlGroup(
      propertySlider(::numLines, r = 1..100),
      propertySlider(::lineSpacing, r = 0.001..200.0)
    ),
    ControlGroup(propertySlider(::noiseScale, r = 1..100)),
    ControlGroup(propertySlider(::moveAmountX, r = 0..2000), propertySlider(::moveAmountY, r = 0..2000)),
    ControlGroup(propertySlider(::seed, r = 0..2000)),
    ControlGroup(property2DSlider(::noiseOffset, Point.One..Point(1000, 1000)), heightRatio = 5)
  )

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  override fun getRandomizedConfig() = WarpConfig()

  private fun drawPoints() = points.forEach { point -> debugCirc(point) }

  private fun getNoiseMovement(p: Point): Point {
    val noisePoint = noiseXY((p * (noiseScale / 100.0)) + (noiseOffset * 2))
    return (noisePoint * Point(moveAmountX, moveAmountY))
  }

  override fun drawOnce(config: WarpConfig) {
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
    val xMax = drawBound.width * xMidpointVal + drawBound.left - (numLines - numLines / 2) * lineSpacing

    val leftWithSomeOverlap = drawBound.leftSegment.expand(moveAmountY * 2)

    if (withHorizontalLines) {
      leftWithSomeOverlap.toProgression(lineSpacing)
        .map { it.y }
        .map { y -> (Point(xMax, y)..Point(xMin, y) step stepsPerPixel).map { p -> p + getNoiseMovement(p) } }
        .forEach { linePoints -> shape(linePoints, drawBound) }
    }

    if (withVerticalLines) {
      for (line in 0..numLines) {
        val x = drawBound.width * xMidpointVal + drawBound.left - (line - numLines / 2) * lineSpacing

        val linePoints = leftWithSomeOverlap.toProgression(stepsPerPixel)
          .map { Point(x, it.y) }
          .map { p -> p + getNoiseMovement(p) }

        shape(linePoints, drawBound)
      }
    }

    rect(drawBound)
  }
}

fun main() = BaseSketch.run(WarpSketch())
