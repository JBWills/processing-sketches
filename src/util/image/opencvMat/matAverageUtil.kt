package util.image.opencvMat

import org.opencv.core.Mat
import util.iterators.meanByOrNull

fun Mat.average(band: Int = 0) =
  toDoubleArray(band)
    .toList()
    .meanByOrNull {
      it.toList()
        .meanByOrNull { d -> d }
        ?: 0
    }
