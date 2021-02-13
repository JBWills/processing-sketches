package util

import coordinate.Point
import java.awt.Color

fun PAppletExt.randomPoint(min: Point, max: Point) =
  Point(random(min.x, max.x), random(min.y, max.y))

fun PAppletExt.randomColor() = Color(random(0f, 255f * 255f * 255f).toInt())
fun PAppletExt.randomLightColor() =
  Color(
    random(100f, 255f).toInt(),
    random(100f, 255f).toInt(),
    random(100f, 255f).toInt()
  )
