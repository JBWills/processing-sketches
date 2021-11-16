package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import util.image.ImageFormat
import util.image.ImageFormat.ARGB
import util.image.ImageFormat.Companion.getFormat
import util.image.converted
import util.numbers.map
import java.awt.Color
import java.nio.ByteBuffer
import java.nio.IntBuffer

fun Mat.copy() = applyWithDest { src, dest -> src.copyTo(dest) }

fun createMat(rows: Int, cols: Int, format: ImageFormat, baseColor: Color) =
  Mat(rows, cols, format.openCVFormat, format.colorToScalar(baseColor))

fun createMat(size: Point, format: ImageFormat, baseColor: Color) =
  createMat(size.yi, size.xi, format, baseColor)

fun BoundRect.toEmptyMat(type: ImageFormat) = Mat(heightPx, widthPx, type.openCVFormat)
fun Mat.asBlankMat(type: Int = type()) = Mat(rows(), cols(), type)

fun Mat.cloneEmpty(format: ImageFormat = getFormat()) = Mat(rows(), cols(), format.openCVFormat)
fun MatOfPoint.cloneEmpty(format: ImageFormat = getFormat()) =
  Mat(rows(), cols(), format.openCVFormat)

fun IntBuffer.copyTo(arr: IntArray): IntBuffer = get(arr)

fun Mat.copyTo(arr: IntArray) {
  asIntBuffer().copyTo(arr)
}

private fun Mat.asIntBuffer(fromFormat: ImageFormat = getFormat()) =
  converted(fromFormat, ARGB)
    .getByteArray()
    .asIntBuffer()

fun Mat.toDoubleArray(bandIndex: Int = 0): Array<DoubleArray> = Array(rows()) { rowIndex ->
  cols()
    .map { colIndex -> get(rowIndex, colIndex)[bandIndex] }
    .toDoubleArray()
}

fun Mat.toIntArray(bandIndex: Int = 0): Array<IntArray> = Array(rows()) { rowIndex ->
  cols()
    .map { colIndex -> get(rowIndex, colIndex)[bandIndex].toInt() }
    .toIntArray()
}

fun Mat.getByteArray(p: Point) = ByteArray(channels()).also { get(p.yi, p.xi, it) }
fun Mat.getByteArray(): ByteArray = ByteArray(width() * height() * channels())
  .also { get(0, 0, it) }

fun ByteArray.toBuffer(): ByteBuffer = ByteBuffer.wrap(this)
fun ByteArray.asIntBuffer(): IntBuffer = toBuffer().asIntBuffer()
