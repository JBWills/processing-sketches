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
  private var lineDegrees: Float = 0f,
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

  private val outerPaddingX: Float = sizeX * 0.05f
  private val outerPaddingY: Float = sizeY * 0.05f
  var drawBound: BoundRect = BoundRect(
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
        lineDegrees = it
        markDirty()
      }
    )
  )

  override fun getRandomizedConfig() = GradientLinesConfig()

  fun bounds(segmentHeight: Float, segIndexFromTop: Int, numSegs: Int = 1) = BoundRect(
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
      distanceBetween = 4f
    )

    drawParallelLinesInBound(
      bounds(1, 1),
      Deg(lineDegrees),
      distanceBetween = 8f
    )

    drawParallelLinesInBound(
      bounds(2, 1),
      Deg(lineDegrees),
      distanceBetween = 16f
    )

    drawParallelLinesInBound(
      bounds(3, 1),
      Deg(lineDegrees),
      distanceBetween = 32f
    )

    drawParallelLinesInBound(
      bounds(4, 1),
      Deg(lineDegrees),
      distanceBetween = 64f
    )

    drawParallelLinesInBound(
      bounds(5, 1),
      Deg(lineDegrees),
      distanceBetween = 128f,
      offset = 32f
    )

    drawParallelLinesInBound(
      bounds(6, 1),
      Deg(lineDegrees),
      distanceBetween = 256f
    )
  }
}

fun main() = BaseSketch.run(GradientLinesSketch())