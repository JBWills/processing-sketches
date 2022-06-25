package util.image.algorithms

import org.opencv.core.Mat
import util.image.opencvMat.get
import util.polylines.PolyLine
import util.polylines.iterators.walk

fun Mat.valuesAlongPath(path: PolyLine, step: Double): List<Double?> =
  path.walk(step, this::get)

fun Mat.valuesAlongPath(path: PolyLine): List<Double?> = path.map(this::get)


fun Mat.averageAlongPath(path: PolyLine, step: Double): Double =
  valuesAlongPath(path, step).filterNotNull().average()
