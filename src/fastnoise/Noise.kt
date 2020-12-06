package fastnoise

import coordinate.Arc
import coordinate.Circ
import coordinate.Point
import coordinate.Segment
import coordinate.Walkable
import fastnoise.NoiseQuality.High
import util.remap

enum class NoiseQuality(val step: Double) {
  Extreme(step = 0.25),
  High(step = 1.0),
  Medium(step = 5.0),
  Low(step = 10.0),
  VeryLow(step = 50.0)
}

fun Point.mapNoiseToPositiveValues() = this + Point(0.5, 0.5)
fun Double.mapNoiseToPositiveValues() = this.remap(-0.5..0.5, 0.0..1.0)

class Noise(
  private val fastNoise: FastNoise,
  val quality: NoiseQuality = High,
  val scale: Double,
  val offset: Point,
  val strength: Point,
) {
  init {
    if (scale !in 0.0..1.0) {
      throw Exception("Scale must be between 0 and 1. Scale: $scale")
    }
  }

  private fun noiseAt2D(p: Point) = Point(
    fastNoise.GetNoise(p.xf, p.yf, 0f),
    fastNoise.GetNoise(p.xf, p.yf, 100f)
  )

  private fun noiseAt(p: Point) = fastNoise.GetNoise(p.xf, p.yf, 0f).toDouble()

  private fun getPointOnNoisePlane(pointInDrawSpace: Point) = (pointInDrawSpace + offset) * scale

  private fun move(p: Point, scaleFn: (Point) -> Point = { it }): Point {
    val noisePoint = noiseAt2D(getPointOnNoisePlane(p))
    return p + scaleFn(noisePoint)
  }

  private fun moveRadially(
    pointToMove: Point,
    originPoint: Point,
    scaleFn: (Double) -> Double = { it },
  ): Point {
    val noiseVal = noiseAt(getPointOnNoisePlane(pointToMove))
    val dir = Segment(originPoint, pointToMove).unitVector
    return pointToMove + (dir * scaleFn(noiseVal))
  }

  fun List<Point>.warped(scaleFn: (Point) -> Point = { it }): List<Point> = map { move(it, scaleFn) }
  fun List<Point>.warpedRadially(originPoint: Point, scaleFn: (Double) -> Double = { it }): List<Point> = map { moveRadially(it, originPoint, scaleFn) }

  fun move(points: List<Point>, scaleFn: (Point) -> Point = { it }): List<Point> = points.warped(scaleFn)
  fun moveRadially(points: List<Point>, originPoint: Point, scaleFn: (Double) -> Double = { it }): List<Point> = points.warpedRadially(originPoint, scaleFn)

  fun warp(w: Walkable, scaleFn: (Point) -> Point = { it }): List<Point> = w.walk(quality.step) { move(it, scaleFn) }
  fun warpRadially(w: Walkable, aroundPoint: Point, scaleFn: (Double) -> Double = { it }): List<Point> = w.walk(quality.step) { moveRadially(it, aroundPoint, scaleFn) }

  companion object {
    fun Walkable.warped(noise: Noise, scaleFn: (Point) -> Point = { it }) = noise.warp(this, scaleFn)
    fun Circ.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) = warpedRadially(noise, origin, scaleFn)
    fun Arc.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) = warpedRadially(noise, origin, scaleFn)
    fun Walkable.warpedRadially(noise: Noise, aroundPoint: Point, scaleFn: (Double) -> Double = { it }) = noise.warpRadially(this, aroundPoint, scaleFn)
  }
}