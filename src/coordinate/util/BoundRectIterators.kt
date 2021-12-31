package coordinate.util

import coordinate.BoundRect
import coordinate.Point
import util.base.step
import util.numbers.sqrt


inline fun <T> BoundRect.mapGrid(block: (Point) -> T) = mapStepped(1.0, 1.0, block)

fun BoundRect.mapPoints(numPoints: Int): List<List<Point>> = mapPoints(numPoints) { it }
fun BoundRect.mapPoints(numPointsX: Int, numPointsY: Int): List<List<Point>> =
  mapPoints(numPointsX, numPointsY) { it }

inline fun <T> BoundRect.mapPoints(numPoints: Int, block: (Point) -> T): List<List<T>> {
  /*
   * w * h = numPX
   * h/w = ratioHW
   * h = ratioHW * w
   * w * (ratioHW * w) = numPX
   * w ^ 2 = numPix / ratioHW
   * w = sqrt(numPix / ratioHW)
   * h = ratioHW * sqrt(numPix / ratioHW)
   */
  val ratioHW: Double = height / width
  val dotsX = (numPoints.toDouble() / ratioHW).sqrt()
  val dotsY = ratioHW * dotsX

  return mapPoints(dotsX, dotsY, block)
}

inline fun <T> BoundRect.mapPoints(numX: Number, numY: Number, block: (Point) -> T) =
  mapSteppedIndexed(
    width / numX.toDouble(),
    height / numY.toDouble(),
  ) { _, point -> block(point) }

inline fun <T> BoundRect.mapStepped(stepX: Number, stepY: Number, block: (Point) -> T) =
  mapSteppedIndexed(stepX, stepY) { _, point -> block(point) }

inline fun <T> BoundRect.mapSteppedIndexed(
  stepX: Number,
  stepY: Number,
  block: (indexes: Pair<Int, Int>, point: Point) -> T
): List<List<T>> =
  (left..right step stepX.toDouble()).mapIndexed { indexX, x ->
    (top..bottom step stepY.toDouble()).mapIndexed { indexY, y ->
      block(indexX to indexY, Point(x, y))
    }
  }
