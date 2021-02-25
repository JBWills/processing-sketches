package util

import appletExtensions.PAppletExt
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun PAppletExt.random(low: Number, high: Number) = random(low.toFloat(), high.toFloat())

fun PAppletExt.randomPoint(min: Point, max: Point) =
  Point(random(min.x, max.x), random(min.y, max.y))

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
    random(100f, 255f).toInt()
  )
