package util

import coordinate.Point

fun PAppletExt.randomPoint(min: Point, max: Point) = Point(random(min.x, max.x), random(min.y, max.y))