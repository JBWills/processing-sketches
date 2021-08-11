package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Core
import org.opencv.core.Mat
import util.letWith

val Mat.size: Point get() = Point(cols(), rows())
val Mat.bounds: BoundRect get() = BoundRect(Point.Zero, size - 1)
val Mat.min: Double get() = minMax.first
val Mat.minMax: Pair<Double, Double> get() = Core.minMaxLoc(this).letWith { minVal to maxVal }
val Mat.max: Double get() = minMax.second

fun Mat.put(bytes: ByteArray, offset: Point = Point.Zero) = put(offset.yi, offset.xi, bytes)
