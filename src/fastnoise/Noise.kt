package fastnoise

import FastNoiseLite
import FastNoiseLite.NoiseType
import FastNoiseLite.NoiseType.Perlin
import arrow.core.memoize
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import coordinate.Segment
import fastnoise.NoiseQuality.High
import interfaces.shape.Walkable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.numbers.remap
import util.polylines.PolyLine
import util.polylines.iterators.walk

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
  val offsetZ: Double = 0.0,
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
    offsetZ: Double? = null,
  ) : this(
    seed ?: noise.seed,
    noiseType ?: noise.noiseType,
    quality ?: noise.quality,
    scale ?: noise.scale,
    Point(offset ?: noise.offset),
    strength = Point(strength ?: noise.strength),
    offsetZ = offsetZ ?: noise.offsetZ,
  )

  constructor(n: Noise) : this(
    n.seed,
    n.noiseType,
    n.quality,
    n.scale,
    n.offset,
    n.strength,
    n.offsetZ,
  )

  /**
   * Get a 2D matrix of noise samples at the pixel level (one sample per pixel)
   */
  fun toValueMatrix(bounds: BoundRect): List<List<Double>> =
    bounds.xPixels.map { x ->
      bounds.yPixels.map { y ->
        get(x, y) + 0.5
      }
    }

  fun with(
    seed: Int? = null,
    noiseType: NoiseType? = null,
    quality: NoiseQuality? = null,
    scale: Double? = null,
    offset: Point? = null,
    strength: Point? = null,
    offsetZ: Number? = null,
  ) = Noise(
    this,
    seed = seed,
    noiseType = noiseType,
    quality = quality,
    scale = scale,
    offset = offset,
    strength = strength,
    offsetZ = offsetZ?.toDouble(),
  )

  private fun noiseAt2D(p: Point, offsetZ: Double = 0.0) = Point(
    fastNoise.GetNoise(p.xf, p.yf, 0f + offsetZ.toFloat()),
    fastNoise.GetNoise(p.xf, p.yf, 100f + offsetZ.toFloat()),
  ) * strength

  private fun noiseAt(p: Point, z: Number = 0.0) =
    fastNoise.GetNoise(p.xf, p.yf, z.toFloat()).toDouble()

  private fun getPointOnNoisePlane(pointInDrawSpace: Point) = (pointInDrawSpace + offset) * scale

  fun get(x: Number, y: Number, z: Number) =
    noiseAt(getPointOnNoisePlane(Point(x, y)), z.toDouble() + offsetZ)

  fun get(x: Number, y: Number) = get(x, y, 0)
  fun get(point: Point) = get(point.x, point.y)
  fun getPositive(x: Number, y: Number, z: Number) = get(x, y, z) + 0.5
  fun getPositive(x: Number, y: Number) = getPositive(x, y, 0)

  private fun move(p: Point, scaleFn: (Point) -> Point = { it }): Point {
    val noisePoint = noiseAt2D(getPointOnNoisePlane(p), offsetZ)
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

  fun PolyLine.warped(scaleFn: (Point) -> Point = { it }): PolyLine =
    map { move(it, scaleFn) }

  fun PolyLine.warpedRadially(
    originPoint: Point, scaleFn: (Double) -> Double = { it },
  ): PolyLine = map { moveRadially(it, originPoint, scaleFn) }

  fun move(points: PolyLine, scaleFn: (Point) -> Point = { it }): PolyLine =
    points.warped(scaleFn)

  fun moveRadially(
    points: PolyLine, originPoint: Point, scaleFn: (Double) -> Double = { it },
  ): PolyLine = points.warpedRadially(originPoint, scaleFn)

  fun warp(w: Walkable, scaleFn: (Point) -> Point = { it }): PolyLine =
    w.walk(quality.step) { move(it, scaleFn) }

  fun warp(w: PolyLine, scaleFn: (Point) -> Point = { it }): PolyLine =
    w.walk(quality.step) { move(it, scaleFn) }

  fun warpRadially(
    w: Walkable,
    aroundPoint: Point,
    scaleFn: (Double) -> Double = { it },
  ): PolyLine = w.walk(quality.step) { moveRadially(it, aroundPoint, scaleFn) }

  fun clone() = Noise(this)

  companion object {
    fun PolyLine.warped(noise: Noise, scaleFn: (Point) -> Point = { it }): PolyLine =
      noise.warp(this, scaleFn)

    fun Walkable.warped(noise: Noise, scaleFn: (Point) -> Point = { it }): PolyLine =
      noise.warp(this, scaleFn)

    val warpedMemo = { walkable: Walkable, noise: Noise ->
      walkable.warped(noise)
    }.memoize()

    fun Circ.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) =
      warpedRadially(noise, origin, scaleFn)

    fun Arc.warpedRadially(noise: Noise, scaleFn: (Double) -> Double = { it }) =
      warpedRadially(noise, origin, scaleFn)

    fun Walkable.warpedRadially(
      noise: Noise, aroundPoint: Point, scaleFn: (Double) -> Double = { it },
    ) = noise.warpRadially(this, aroundPoint, scaleFn)

    val DEFAULT
      get() = Noise(
        seed = 100,
        noiseType = Perlin,
        quality = High,
        scale = 0.15,
        offset = Point.Zero,
        strength = Point(0, 0),
      )
  }
}
