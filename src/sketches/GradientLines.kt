package sketches

import appletExtensions.drawParallelLinesInBound
import BaseSketch
import SketchConfig
import controls.Control
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import java.awt.Color

class GradientLinesConfig : SketchConfig()

open class GradientLinesSketch(
  private var lineDegrees: Double = 0.0,
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864
) : BaseSketch<GradientLinesConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "sketches.GradientLines",
  sketchConfig = null,
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
  )

  override fun getControls() = listOf(
    Control.Slider(
      text = "Line angle (degrees)",
      range = 0.0..360.0,
      handleChange =
      {
        lineDegrees = it
        markDirty()
      }
    )
  )

  override fun getRandomizedConfig() = GradientLinesConfig()

  fun bounds(segmentHeight: Double, segIndexFromTop: Int, numSegs: Int = 1) = BoundRect(
    drawBound.topLeft + Point(0f, segIndexFromTop * segmentHeight),
    segmentHeight * numSegs,
    drawBound.width
  )

  override fun drawOnce(config: GradientLinesConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()

    val segmentHeight = drawBound.height / 7f

    fun bounds(segIndexFromTop: Int, numSegs: Int = 1) = bounds(segmentHeight, segIndexFromTop, numSegs)

    rect(drawBound)
    drawParallelLinesInBound(
      bounds(0, 1),
      Deg(lineDegrees),
      distanceBetween = 4
    )

    drawParallelLinesInBound(
      bounds(1, 1),
      Deg(lineDegrees),
      distanceBetween = 8
    )

    drawParallelLinesInBound(
      bounds(2, 1),
      Deg(lineDegrees),
      distanceBetween = 16
    )

    drawParallelLinesInBound(
      bounds(3, 1),
      Deg(lineDegrees),
      distanceBetween = 32
    )

    drawParallelLinesInBound(
      bounds(4, 1),
      Deg(lineDegrees),
      distanceBetween = 64
    )

    drawParallelLinesInBound(
      bounds(5, 1),
      Deg(lineDegrees),
      distanceBetween = 128,
      offset = 32
    )

    drawParallelLinesInBound(
      bounds(6, 1),
      Deg(lineDegrees),
      distanceBetween = 256f
    )
  }
}

fun main() = BaseSketch.run(GradientLinesSketch())