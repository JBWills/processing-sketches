package util.image.opencvMat

import arrow.core.memoize
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import util.image.opencvMat.flags.ImreadFlags

fun loadImageMat(path: String, flags: ImreadFlags = ImreadFlags.ImreadUnchanged): Mat =
  Imgcodecs.imread(path, flags.value)

val loadImageMatMemo = { path: String, flags: ImreadFlags -> loadImageMat(path, flags) }.memoize()
