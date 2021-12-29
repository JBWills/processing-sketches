package util

import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.base.DoubleRange
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val DefaultSeed = 0

private val seedToRandomInstance: ConcurrentHashMap<Int, Random> = ConcurrentHashMap()

fun getRandomInstance(seed: Int = DefaultSeed): Random {
  return seedToRandomInstance.getOrPut(seed) { Random(seed) }
}

fun rand(seed: Int = DefaultSeed): Double = getRandomInstance(seed).nextDouble()
fun randomInt(range: IntRange, seed: Int = DefaultSeed): Int =
  if (range.first == range.last) range.first
  else getRandomInstance(seed).nextInt(from = range.first, until = range.last)

fun randomDouble(range: DoubleRange, seed: Int = DefaultSeed) =
  if (range.start == range.endInclusive) range.start
  else getRandomInstance(seed).nextDouble(from = range.start, until = range.endInclusive)

fun BoundRect.randomPoint(seed: Int = DefaultSeed) =
  Point(randomDouble(left..right), randomDouble(top..bottom))

@JvmName("randomPointFunctional")
fun randomPoint(boundRect: BoundRect, seed: Int = DefaultSeed) = boundRect.randomPoint(seed)

@JvmName("randomPointFunctional")
fun randomPoint(c: Circ, onCircumferenceOnly: Boolean = false, seed: Int = DefaultSeed): Point =
  c.randomPoint(onCircumferenceOnly, seed)

fun Circ.randomPoint(onCircumferenceOnly: Boolean = false, seed: Int = DefaultSeed): Point {
  val a: Double = rand(seed) * 2 * PI
  val r: Double = radius * if (onCircumferenceOnly) 1.0 else sqrt(rand(seed))

  return origin + Point(r * cos(a), r * sin(a))
}

fun Point.translatedRandomDirection(dist: Double, seed: Int = DefaultSeed): Point {
  if (dist == 0.0) return this
  val a: Double = rand(seed) * 2 * PI

  return this + Point(dist * cos(a), dist * sin(a))
}

fun randomColor() = Color(randomDouble(0.0..(255.0 * 255.0 * 255.0)).toInt())
fun randomLightColor() =
  Color(
    randomDouble(100.0..255.0).toInt(),
    randomDouble(100.0..255.0).toInt(),
    randomDouble(100.0..255.0).toInt(),
  )
