package sketches

import appletExtensions.drawParallelLinesInBound
import BaseSketch
import coordinate.Deg
import java.awt.Color

class GradientLinesDiagonalSketch(
  private var lineDegrees: Double = 44.0,
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : GradientLinesSketch(lineDegrees, isDebugMode, backgroundColor, sizeX, sizeY) {
  override fun getRandomizedConfig() = GradientLinesConfig()

  override fun drawOnce(config: GradientLinesConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(1f)
    noFill()

    val segmentHeight = drawBound.height / 7.0

    fun bounds(segIndexFromTop: Int, numSegs: Int = 1) = bounds(segmentHeight, segIndexFromTop, numSegs)

    rect(drawBound)
    drawParallelLinesInBound(
      bounds(0, 1),
      Deg(lineDegrees),
      distanceBetween = 8f,
      offset = 4f
    )

    drawParallelLinesInBound(
      bounds(0, 2),
      Deg(lineDegrees),
      distanceBetween = 16f,
      offset = 8f
    )

    drawParallelLinesInBound(
      bounds(0, 3),
      Deg(lineDegrees),
      distanceBetween = 32f,
      offset = 16f
    )

    drawParallelLinesInBound(
      bounds(0, 4),
      Deg(lineDegrees),
      distanceBetween = 64f,
      offset = 32f
    )

    drawParallelLinesInBound(
      bounds(0, 5),
      Deg(lineDegrees),
      distanceBetween = 128f,
      offset = 64f
    )

    drawParallelLinesInBound(
      bounds(0, 6),
      Deg(lineDegrees),
      distanceBetween = 128f
    )
  }
}

fun main() = BaseSketch.run(GradientLinesDiagonalSketch())