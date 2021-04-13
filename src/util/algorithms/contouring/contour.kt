package util.algorithms.contouring

import arrow.core.memoize
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

private fun bools(
  all: Boolean? = null,
  top: Boolean? = null,
  bottom: Boolean? = null,
  left: Boolean? = null,
  right: Boolean? = null,
  topLeft: Boolean? = null,
  bottomLeft: Boolean? = null,
  topRight: Boolean? = null,
  bottomRight: Boolean? = null,
): List<Boolean> = listOf(
  topLeft ?: top ?: left ?: all ?: false,
  topRight ?: top ?: right ?: all ?: false,
  bottomRight ?: bottom ?: right ?: all ?: false,
  bottomLeft ?: bottom ?: left ?: all ?: false,
)

val LOOKUP_TABLE: Map<List<Boolean>, (leftMidPoint: Point, rightMidpoint: Point, topMidpoint: Point, bottomMidpoint: Point) -> List<Segment>> =
  mapOf(
    bools(all = false) to { _, _, _, _ -> listOf() },
    bools(all = true) to { _, _, _, _ -> listOf() },
    bools(bottomLeft = true) to { l, _, _, b -> listOf(Segment(l, b)) },
    bools(all = true, bottomLeft = false) to { l, _, _, b ->
      listOf(Segment(l, b))
    },
    bools(bottomRight = true) to { _, r, _, b ->
      listOf(Segment(r, b))
    },
    bools(
      all = true,
      bottomRight = false
    ) to { _, r, _, b ->
      listOf(Segment(r, b))
    },
    bools(topRight = true) to { _, r, t, _ -> listOf(Segment(t, r)) },
    bools(all = true, topRight = false) to { _, r, t, _ ->
      listOf(Segment(t, r))
    },
    bools(topLeft = true) to { l, _, t, _ -> listOf(Segment(t, l)) },
    bools(all = true, topLeft = false) to { l, _, t, _ ->
      listOf(Segment(t, l))
    },
    bools(top = true) to { l, r, _, _ -> listOf(Segment(l, r)) },
    bools(bottom = true) to { l, r, _, _ -> listOf(Segment(l, r)) },
    bools(left = true) to { _, _, t, b -> listOf(Segment(t, b)) },
    bools(right = true) to { _, _, t, b -> listOf(Segment(t, b)) },
    bools(
      topRight = true,
      bottomLeft = true
    ) to { l, r, t, b ->
      listOf(
        Segment(t, l),
        Segment(r, b)
      )
    },
    bools(
      topLeft = true,
      bottomRight = true
    ) to { l, r, t, b ->
      listOf(
        Segment(t, r),
        Segment(l, b)
      )
    },
  )

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
        val pointsIn = bools(
          topLeft = vF(topLeft) > threshold, // topLeft
          topRight = vF(topRight) > threshold, // topRight
          bottomLeft = vF(bottomLeft) > threshold, // bottomLeft
          bottomRight = vF(bottomRight) > threshold, // bottomRight
        )

        points.getValue(threshold).addAll(
          LOOKUP_TABLE[pointsIn]?.invoke(
            Segment(topLeft, bottomLeft).midPoint.bound(xRange, yRange),
            Segment(topRight, bottomRight).midPoint.bound(xRange, yRange),
            Segment(topLeft, topRight).midPoint.bound(xRange, yRange),
            Segment(bottomLeft, bottomRight).midPoint.bound(xRange, yRange),
          )
            ?: listOf()
        )
      }
    }
  }

  return points
}


/**
 * @param step the sample rate along bound.
 */
val getNoiseContour = {
    thresholds: List<Double>,
    bound: BoundRect,
    step: Double,
    noise: Noise,
  ->
  getContour(
    thresholds,
    step,
    xRange = bound.left..bound.right,
    yRange = bound.top..bound.bottom,
  ) { x, y ->
    (noise.get(x, y) + 0.5)
  }
}.memoize()


