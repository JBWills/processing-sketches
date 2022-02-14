package util.image.opencvMat.debug

import org.opencv.core.Mat
import util.image.opencvMat.average
import util.numbers.map

fun Mat.printDebug(prefix: String? = null, suffix: String? = null) {
  print(
    """
    ${prefix ?: ""}
    Mat: w=${width()} h=${height()} channels=${channels()}
    Average per-channel value: ${channels().map { average(it) }}
    ${suffix ?: ""}
  """,
  )
}
