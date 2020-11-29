package util

import coordinate.Point

typealias PointRange = ClosedRange<Point>

val PointRange.xRange get() = start.x..endInclusive.x
val PointRange.yRange get() = start.y..endInclusive.y