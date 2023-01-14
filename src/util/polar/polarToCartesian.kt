package util.polar

import coordinate.Point
import coordinate.times
import kotlin.math.cos
import kotlin.math.sin

fun polarToPoint(theta: Double, length: Double): Point = length * Point(cos(theta), sin(theta))
