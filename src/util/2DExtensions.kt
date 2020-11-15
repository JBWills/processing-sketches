package util

import coordinate.Point
import java.awt.geom.Point2D

fun Point2D.toPoint() = Point(x, y)
fun Point2D.Float.toPoint() = Point(x, y)
fun Point2D.Double.toPoint() = Point(x, y)

val Point2D.xf get() = x
val Point2D.yf get() = y
