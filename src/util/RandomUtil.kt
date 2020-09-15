package util

import coordinate.Point
import processing.core.PApplet

fun PApplet.randomPoint(min: Point, max: Point) = Point(random(min.x, max.x), random(min.y, max.y))