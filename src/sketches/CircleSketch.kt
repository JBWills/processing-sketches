package sketches

import BaseSketch
import SketchConfig
import coordinate.Point
import util.times
import java.awt.Color

data class CircleConfig(
  val distanceBetween: Float,
  val circleLimit: Int,
  val originPointsX: Int,
  val originPointsY: Int,
) : SketchConfig() {
  override fun toString(): String {
    return "CircleConfigValues(distanceBetween=$distanceBetween, circleLimit=$circleLimit, originPointsX=$originPointsX, originPointsY=$originPointsY)"
  }
}

class CircleSketch : BaseSketch<CircleConfig>(
  backgroundColor = Color.white,
  svgBaseFileName = "sketches.CircleSketch",
  sketchConfig = null,
) {
  override fun getRandomizedConfig() = CircleConfig(
    distanceBetween = random(49, 50),
    circleLimit = random(1, 20).toInt(),
    originPointsX = random(5, 10).toInt(),
    originPointsY = random(5, 50).toInt()
  )

  override fun drawOnce(config: CircleConfig) {
    fun drawCircle(center: Point, size: Number, strokeColor: Color = Color.black) {
      stroke(strokeColor.rgb)
      strokeWeight(0.5f)
      noFill()
      circle(center.x, center.y, size.toFloat())
    }

    fun drawConcentricCircles(limit: Int, center: Point, distanceBetween: Float, strokeColor: Color = Color.black) {
      val numToDraw = max((max(sizeX, sizeY) * 4 / distanceBetween).toInt(), limit)
      numToDraw.times { i -> drawCircle(center, (i.toFloat() * distanceBetween), strokeColor) }
    }

    val center = Point(sizeX / 2, sizeY / 2)

    noStroke()
    config.originPointsX.times { xIndex ->
      drawConcentricCircles(
        config.circleLimit,
        center + Point(sizeX * 3f * (xIndex / 20f) - sizeX, 0f),
        config.distanceBetween, Color.BLACK)
    }
  }
}

fun main() = BaseSketch.run(CircleSketch())