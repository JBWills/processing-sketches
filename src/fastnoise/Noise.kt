package fastnoise

import FastNoiseLite
import FastNoiseLite.NoiseType
import coordinate.Arc
import coordinate.Circ
import coordinate.Point
import coordinate.Segment
import fastnoise.NoiseQuality.High
import interfaces.shape.Walkable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.remap

@Suppress("unused")
enum class NoiseQuality(val step: Double) {
  Extreme(step = 0.25),
  High(step = 1.0),
  Medium(step = 5.0),
  Low(step = 10.0),
  VeryLow(step = 50.0)
}

fun Point.mapNoiseToPositiveValues() = this + Point(0.5, 0.5)
fun Double.mapNoiseToPositiveValues() = this.remap(-0.5..0.5, 0.0..1.0)

@Serializable
data class Noise(
  val seed: Int,
  val noiseType: NoiseType,
  val quality: NoiseQuality = High,
  val scale: Double,
  val offset: Point,
  val strength: Point,
) {

  @Transient
  val fastNoise: FastNoiseLite = createFastNoise(seed, noiseType)

  constructor(
    noise: Noise,
    seed: Int? = null,
    noiseType: NoiseType? = null,
    quality: NoiseQuality? = null,
    scale: Double? = null,
    offset: Point? = null,
    strength: Point? = null,
  ) : this(
    seed ?: noise.seed,
    noiseType ?: noise.noiseType,
    quality ?: noise.quality,
    scale ?: noise.scale,
    Point(offset ?: noise.offset),
    Point(strength ?: noise.strength)
  )

  constructor(n: Noise) : this(
    n.seed,
    n.noiseType,
    n.quality,
    n.scale,
    n.offset,
    n.strength
  )

  fun with(
    seed: Int? = null,
    noiseType: NoiseType? = null,
    quality: NoiseQuality? = null,
    scale: Double? = null,
    offset: Point? = null,
    strength: Point? = null,
  ) = Noise(this, seed = seed, noiseType = noiseType, quality = quality, scale = scale,
    offset = offset, strength = strength)

  private fun noiseAt2D(p: Point) = Point(
    fastNoise.GetNoise(p.xf, p.yf, 0f),
    fastNoise.GetNoise(p.xf, p.yf, 100f)
  ) * strength

  fun noiseAt(p: Point) = fastNoise.GetNoise(p.xf, p.yf, 0f).toDouble()

  fun getPointOnNoisePlane(pointInDrawSpace: Point) = (pointInDrawSpace + offset) * scale

  private fun move(p: Point, scaleFn: (Point) -> Point = { it }): Point {
    val noisePoint = noiseAt2D(getPointOnNoisePlane(p))
    return p + scaleFn(noisePoint)
  }

  fun moveRadially(
    pointToMove: Point,
    originPoint: Point,
    scaleFn: (Double) -> Double = { it },
  ): Point {
    val noiseVal = noiseAt(getPointOnNoisePlane(pointToMove))
    val dir = Segment(originPoint, pointToMove).unitVector
    return pointToMove + (dir * scaleFn(noiseVal))
  }

  fun List<Point>.warped(scaleFn: (Point) -> Point = { it }): List<Point> =
    map { move(it, scaleFn) }

  fun List<Point>.warpedRadially(
    originPoint: Point, scaleFn: (Double) -> Double = { it },
  ): List<Point> = map { moveRadially(it, originPoint, scaleFn) }

  fun move(points: List<Point>, scaleFn: (Point) -> Point = { it }): List<Point> =
    points.warped(scaleFn)

  fun moveRadially(
    points: List<Point>, originPoint: Point, scaleFn: (Double) -> Double = { it },
  ): List<Point> = points.warpedRadially(originPoint, scaleFn)

  fun warp(w: Walkable, scaleFn: (Point) -> Point = { it }): List<Point> =
    w.walk(quality.step) { move(it, scaleFn) }

  fun warpRadially(
    w: Walkable, aroundPoint: Point, scaleFn: (Double) -> Double = { it },
  ): List<Point> = w.walk(quality.step) { moveRadially(it, aroundPoint, scaleFn) }

  fun clone() = Noise(this)

  companion object {
    fun Walkable.warped(noise: Noise, scaleFn: (Point) -> Point = { it }) =
      noise.warp(this, scaleFn)

    fun Circ.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) =
      warpedRadially(noise, origin, scaleFn)

    fun Arc.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) =
      warpedRadially(noise, origin, scaleFn)

    fun Walkable.warpedRadially(
      noise: Noise, aroundPoint: Point, scaleFn: (Double) -> Double = { it },
    ) = noise.warpRadially(this, aroundPoint, scaleFn)
  }

}
