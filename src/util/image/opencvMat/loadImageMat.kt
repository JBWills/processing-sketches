package util.image.opencvMat

import arrow.core.memoize
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import util.image.opencvMat.flags.ImreadFlags
import java.io.File

fun loadImageMat(path: String, flags: ImreadFlags = ImreadFlags.ImreadUnchanged): Mat? =
  if (File(path).exists()) Imgcodecs.imread(path, flags.value) else null

val loadImageMatMemo = { path: String, flags: ImreadFlags -> loadImageMat(path, flags) }.memoize()
