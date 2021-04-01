package util.algorithms.contouring

import coordinate.BoundRect
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import util.DoubleRange
import util.step
import kotlin.math.sign

/**
 * From https://wordsandbuttons.online/the_simplest_possible_smooth_contouring_algorithm.html
 */
fun getJaggedContour(
  step: Double,
  xRange: DoubleRange,
  yRange: DoubleRange,
  valueFunction: (Double, Double) -> Double
): List<Segment> {
  val points: MutableList<Segment> = mutableListOf()
  (yRange step step).forEach { y ->
    (xRange step step).forEach { x ->
      val d_in_square = valueFunction(x + step / 2, y + step / 2)
      val d_in_square_on_the_left = valueFunction(x - step / 2, y + step / 2)
      val d_in_square_above = valueFunction(x + step / 2, y - step / 2)
      val currPoint = Point(x, y)

      // draw a horizontal piece if there is a sign change
      if (d_in_square.sign != d_in_square_above.sign && x != xRange.endInclusive) {
        points.add(Segment(currPoint, currPoint.addX(step).boundX(xRange)))
      }

      // draw a vertical piece if there is a sign change
      if (d_in_square.sign != d_in_square_on_the_left.sign && y != xRange.endInclusive) {
        points.add(Segment(currPoint, currPoint.addY(step).boundY(yRange)))
      }
    }
  }

  return points
}

/**
 * From https://wordsandbuttons.online/the_simplest_possible_smooth_contouring_algorithm.html
 */
fun getContour(
  thresholds: List<Double>,
  grd_size: Double,
  xRange: DoubleRange,
  yRange: DoubleRange,
  vF: (Double, Double) -> Double,
): Map<Double, List<Segment>> {
  val points: MutableMap<Double, MutableList<Segment>> =
    thresholds.map { it to mutableListOf<Segment>() }.toMap().toMutableMap()

  fun vF(p: Point) = vF(p.x, p.y)

  (yRange step grd_size).forEach { y ->
    (xRange step grd_size).forEach { x ->
      val p = Point(x, y)

      val topLeft = p + Point(-(grd_size / 2), -(grd_size / 2))
      val topRight = p + Point((grd_size / 2), -(grd_size / 2))
      val bottomRight = p + Point((grd_size / 2), (grd_size / 2))
      val bottomLeft = p + Point(-(grd_size / 2), (grd_size / 2))

      thresholds.forEach { threshold ->
        val pointsIn = listOf(
          vF(topLeft) > threshold, // topLeft
          vF(topRight) > threshold, // topRight
          vF(bottomLeft) > threshold, // bottomLeft
          vF(bottomRight) > threshold, // bottomRight
        )

        val leftMidpoint = Segment(topLeft, bottomLeft).midPoint.bound(xRange, yRange)
        val rightMidpoint = Segment(topRight, bottomRight).midPoint.bound(xRange, yRange)
        val bottomMidpoint = Segment(bottomLeft, bottomRight).midPoint.bound(xRange, yRange)
        val topMidpoint = Segment(topLeft, topRight).midPoint.bound(xRange, yRange)

        points.getValue(threshold).addAll(
          when (pointsIn) {
            listOf(
              false, false,
              false, false
            ),
            listOf(
              true, true,
              true, true
            ) -> listOf()

            listOf(
              false, false,
              true, false
            ), listOf(
              true, true,
              false, true
            ) -> listOf(Segment(leftMidpoint, bottomMidpoint))

            listOf(
              false, false,
              false, true
            ), listOf(
              true, true,
              true, false
            ) -> listOf(Segment(rightMidpoint, bottomMidpoint))

            listOf(
              false, true,
              false, false
            ), listOf(
              true, false,
              true, true
            ) -> listOf(Segment(topMidpoint, rightMidpoint))

            listOf(
              false, true,
              true, true
            ),
            listOf(
              true, false,
              false, false
            ) -> listOf(Segment(topMidpoint, leftMidpoint))

            listOf(
              false, false,
              true, true
            ), listOf(
              true, true,
              false, false,
            ) -> listOf(Segment(leftMidpoint, rightMidpoint))

            listOf(
              false, true,
              false, true
            ), listOf(
              true, false,
              true, false
            ) -> listOf(Segment(topMidpoint, bottomMidpoint))

            listOf(
              false, true,
              true, false
            ) -> listOf(Segment(topMidpoint, leftMidpoint), Segment(rightMidpoint, bottomMidpoint))

            listOf(
              true, false,
              false, true
            ) -> listOf(Segment(topMidpoint, leftMidpoint), Segment(rightMidpoint, bottomMidpoint))


            else -> listOf()
          }
        )
      }
    }
  }

  return points
}


/**
 * @param step the sample rate along bound.
 */
fun getNoiseContour(
  thresholds: List<Double>,
  bound: BoundRect,
  step: Double,
  noise: Noise,
) = getContour(
  thresholds,
  step,
  xRange = bound.left..bound.right,
  yRange = bound.top..bound.bottom,
) { x, y ->
  (noise.get(x, y) + 0.5)
}

