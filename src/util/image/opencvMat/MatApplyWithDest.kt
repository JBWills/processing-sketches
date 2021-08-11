package util.image.opencvMat

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat

/**
 * Helper to apply a function to a mat with a newly created destination mat.
 *
 * @param format the image format of the new mat.
 * @param block the operation to perform
 * @return the new mat.
 */
fun Mat.applyWithDest(
  format: ImageFormat = getFormat(),
  inPlace: Boolean = false,
  block: (src: Mat, dest: Mat) -> Unit,
): Mat = (if (inPlace) this else cloneEmpty(format)).also { dest -> block(this, dest) }

/**
 * Helper to apply a function to a mat with a newly created destination mat.
 *
 * @param format the image format of the new mat.
 * @param block the operation to perform
 * @return the new mat.
 */
fun MatOfPoint2f.applyWithDestPoints(
  inPlace: Boolean = false,
  block: (src: MatOfPoint2f, dest: MatOfPoint2f) -> Unit,
): MatOfPoint2f = (if (inPlace) this else MatOfPoint2f()).also { dest -> block(this, dest) }
