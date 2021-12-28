package util

import appletExtensions.PAppletExt
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import util.base.DoubleRange
import java.awt.Color
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun randomInt(range: IntRange): Int =
  ((random() * (range.last - range.first)) + range.first).toInt()

fun random(range: DoubleRange) = (random() * (range.endInclusive - range.start)) + range.start

fun PAppletExt.random(low: Number, high: Number) = random(low.toFloat(), high.toFloat())

fun PAppletExt.randomPoint(min: Point, max: Point) =
  Point(random(min.x, max.x), random(min.y, max.y))

fun BoundRect.randomPoint() = Point(random(left..right), random(top..bottom))

fun PAppletExt.randomPoint(boundRect: BoundRect) =
  randomPoint(boundRect.topLeft, boundRect.bottomRight)

fun PAppletExt.randomPoint(c: Circ): Point {
  val a: Double = random(0, 1) * 2 * PI
  val r: Double = c.radius * sqrt(random(0, 1))

  return c.origin + Point(r * cos(a), r * sin(a))
}

fun PAppletExt.randomColor() = Color(random(0f, 255f * 255f * 255f).toInt())
fun PAppletExt.randomLightColor() =
  Color(
    random(100f, 255f).toInt(),
    random(100f, 255f).toInt(),
    random(100f, 255f).toInt(),
  )
