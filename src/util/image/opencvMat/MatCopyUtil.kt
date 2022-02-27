package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.bytesAndBuffers.toDoubleArray
import util.image.bytesAndBuffers.toIntArray
import util.image.opencvMat.ChannelDepth.Companion.channelDepth
import util.iterators.filterNotNull
import util.numbers.times
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

fun Mat.toIntArray(bandIndex: Int = 0): Array<IntArray> {
  val singleBandMat = split().getOrNull(bandIndex) ?: return arrayOf()

  val result = Array<IntArray?>(rows()) { null }
  rows().times { rowIndex ->
    val row = singleBandMat.row(rowIndex)

    result[rowIndex] = row.getByteArray().toIntArray(row.channelDepth().byteDepth)
  }

  return result.filterNotNull()
}

fun Mat.toDoubleArray(bandIndex: Int = 0): Array<DoubleArray> {
  val singleBandMat = split().getOrNull(bandIndex) ?: return arrayOf()

  val result = Array<DoubleArray?>(rows()) { null }
  rows().times { rowIndex ->
    val row = singleBandMat.row(rowIndex)

    result[rowIndex] = row.getByteArray().toDoubleArray(row.channelDepth().byteDepth)
  }

  return result.filterNotNull()
}


fun Mat.getByteArray(p: Point) =
  ByteArray(channels()).also { get(p.yi, p.xi, it) }

fun Mat.getByteArray(): ByteArray = ByteArray(width * height * channels())
  .also { get(0, 0, it) }

fun ByteArray.toBuffer(): ByteBuffer = ByteBuffer.wrap(this)
fun ByteArray.asIntBuffer(): IntBuffer = toBuffer().asIntBuffer()
