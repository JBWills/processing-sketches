package util.random

import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.base.DoubleRange
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun <T> Random.randItem(l: List<T>): T =
  randItemOrNull(l) ?: throw Exception("Trying to get a random item from an empty list")

fun <T> Random.randItem(arr: Array<T>): T =
  randItemOrNull(arr) ?: throw Exception("Trying to get a random item from an empty list")

fun <T> Random.randItemOrNull(l: List<T>): T? =
  if (l.isEmpty()) null
  else l[randomInt(l.indices)]

fun <T> Random.randItemOrNull(arr: Array<T>): T? =
  if (arr.isEmpty()) null
  else arr[randomInt(arr.indices)]

fun Random.randomInt(range: IntRange): Int =
  if (range.first >= range.last) range.first
  else nextInt(from = range.first, until = range.last)

fun Random.randomDouble(range: DoubleRange) =
  if (range.start >= range.endInclusive) range.start
  else nextDouble(from = range.start, until = range.endInclusive)

fun Random.randomPoint(boundRect: BoundRect) =
  Point(
    randomDouble(boundRect.left..boundRect.right),
    randomDouble(boundRect.top..boundRect.bottom),
  )

fun Random.randomPoint(circ: Circ, onCircumferenceOnly: Boolean = false): Point {
  val a: Double = nextDouble() * 2 * PI
  val r: Double = circ.radius * if (onCircumferenceOnly) 1.0 else sqrt(nextDouble())

  return circ.origin + Point(r * cos(a), r * sin(a))
}

fun Random.translatedRandomDirection(point: Point, dist: Double): Point =
  randomPoint(Circ(point, dist), onCircumferenceOnly = true)

fun Random.randomColor() = Color(randomDouble(0.0..(255.0 * 255.0 * 255.0)).toInt())
fun Random.randomLightColor() =
  Color(
    randomInt(100..255),
    randomInt(100..255),
    randomInt(100..255),
  )
