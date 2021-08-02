package util.image.raster

import coordinate.BoundRect
import coordinate.Point
import java.awt.image.Raster

val Raster.aspect: Double get() = width.toDouble() / height
val Raster.boundRect: BoundRect get() = BoundRect(width, height)
val Raster.center: Point get() = Point(width / 2, height / 2)
val Raster.size: Point get() = Point(width, height)

fun Raster.getSampleDouble(p: Point) = getSampleDouble(p.x.toInt(), p.y.toInt(), 0)
fun Raster.getSampleDoubleRelaxed(p: Point) = getSampleDouble(p.x.toInt(), p.y.toInt(), 0)
