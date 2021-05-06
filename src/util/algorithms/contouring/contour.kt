package util.algorithms.contouring

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import util.DoubleRange
import util.iterators.zipNullPadded
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

private typealias MidpointsToSegments = (
  f: (Point) -> Boolean,
  boundBox: BoundRect,
) -> List<Segment>

private fun sWithCommonPointOrDirection(s1: Segment, s2: Segment): Pair<Segment, Segment> =
  when {
    s1.p1 == s2.p2 -> s1 to s2.flip()
    s1.p2 == s2.p1 -> s1.flip() to s2
    s1.isParallel(s2) -> s1.withReorientedDirection(s2) to s2
    else -> s1 to s2
  }

private fun shouldFlipDirection(s1: Segment, s2: Segment): Pair<Boolean, Boolean> =
  when {
    s1.p1 == s2.p2 -> false to true
    s1.p2 == s2.p1 -> true to false
    s1.isParallel(s2) -> (s1.p1 != s2.p1) to false
    else -> false to false
  }

private fun s(s1: Segment, s2: Segment, f: (Point) -> Boolean): List<Segment> {
  val (s1ShouldFlip, s2ShouldFlip) = shouldFlipDirection(s1, s2)

  val s1Points = if (s1ShouldFlip) s1.pointsAtThreshold(f).reversed() else s1.pointsAtThreshold(f)
  val s2Points = if (s2ShouldFlip) s2.pointsAtThreshold(f).reversed() else s2.pointsAtThreshold(f)

  return (s1Points to s2Points)
    .zipNullPadded { p1, p2 ->
      Segment(
        p1 ?: s1Points.last(),
        p2 ?: s2Points.last(),
      )
    }
}

private val LOOKUP_TABLE: Map<List<Boolean>, MidpointsToSegments> = mapOf(
  bools(all = false)
    to { f, bound -> listOf() },
  bools(all = true)
    to { f, bound -> listOf() },
  bools(bottomLeft = true)
    to { f, bound -> s(bound.leftSegment, bound.bottomSegment, f) },
  bools(all = true, bottomLeft = false)
    to { f, bound -> s(bound.leftSegment, bound.bottomSegment, f) },
  bools(bottomRight = true)
    to { f, bound -> s(bound.rightSegment, bound.bottomSegment, f) },
  bools(all = true, bottomRight = false)
    to { f, bound -> s(bound.rightSegment, bound.bottomSegment, f) },
  bools(topRight = true)
    to { f, bound -> s(bound.topSegment, bound.rightSegment, f) },
  bools(all = true, topRight = false)
    to { f, bound -> s(bound.topSegment, bound.rightSegment, f) },
  bools(topLeft = true)
    to { f, bound -> s(bound.topSegment, bound.leftSegment, f) },
  bools(all = true, topLeft = false)
    to { f, bound -> s(bound.topSegment, bound.leftSegment, f) },
  bools(top = true)
    to { f, bound -> s(bound.leftSegment, bound.rightSegment, f) },
  bools(bottom = true)
    to { f, bound -> s(bound.leftSegment, bound.rightSegment, f) },
  bools(left = true)
    to { f, bound -> s(bound.topSegment, bound.bottomSegment, f) },
  bools(right = true)
    to { f, bound -> s(bound.topSegment, bound.bottomSegment, f) },
  bools(topRight = true, bottomLeft = true)
    to { f, bound ->
    s(bound.topSegment, bound.leftSegment, f) + s(bound.rightSegment, bound.bottomSegment, f)
  },
  bools(topLeft = true, bottomRight = true)
    to { f, bound ->
    s(bound.topSegment, bound.rightSegment, f) + s(bound.leftSegment, bound.bottomSegment, f)
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
        fun isVf(p: Point) = vF(p) > threshold
        val pointsIn = bools(
          topLeft = isVf(topLeft), // topLeft
          topRight = isVf(topRight), // topRight
          bottomLeft = isVf(bottomLeft), // bottomLeft
          bottomRight = isVf(bottomRight), // bottomRight
        )

        points.getValue(threshold).addAll(
          LOOKUP_TABLE[pointsIn]?.invoke(
            { point -> isVf(point) },
            BoundRect(topLeft, bottomRight),
          )
            ?: listOf(),
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
