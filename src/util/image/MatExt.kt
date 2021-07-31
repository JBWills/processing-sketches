package util.image

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvContouring.applyWithDest
import util.image.opencvContouring.submat
import util.isAllUniqueChars
import util.letWith
import util.map
import util.tuple.Pair3
import util.tuple.Pair4
import java.awt.Color
import java.nio.ByteBuffer
import java.nio.IntBuffer

val Mat.size: Point get() = Point(cols(), rows())
val Mat.bounds: BoundRect get() = BoundRect(Point.Zero, size - 1)
val Mat.min: Double get() = minMax.first
val Mat.minMax: Pair<Double, Double> get() = Core.minMaxLoc(this).letWith { minVal to maxVal }
val Mat.max: Double get() = minMax.second

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


fun Mat.contains(p: Point) = bounds.contains(p)

fun Mat.get(p: Point, band: Int = 0): Double? =
  get(p.y.toInt(), p.x.toInt())?.get(band)?.let { if (it.isNaN()) null else it }

fun Mat.getOr(p: Point, default: Double, band: Int = 0): Double = get(p, band) ?: default
fun createMat(rows: Int, cols: Int, format: ImageFormat, baseColor: Color) =
  Mat(rows, cols, format.openCVFormat, format.colorToScalar(baseColor))

fun BoundRect.toEmptyMat(type: ImageFormat) = Mat(heightPx, widthPx, type.openCVFormat)

fun ByteArray.toBuffer(): ByteBuffer = ByteBuffer.wrap(this)
fun ByteArray.asIntBuffer(): IntBuffer = toBuffer().asIntBuffer()

fun IntBuffer.copyTo(arr: IntArray) = get(arr)

fun Mat.asBlankMat(type: Int = type()) = Mat(rows(), cols(), type)

fun Mat.split(): List<Mat> {
  val mats = mutableListOf<Mat>()
  Core.split(this, mats)
  return mats
}

fun Mat.splitRgb(): Pair3<Mat, Mat, Mat> {
  if (channels() != 3) {
    throw Exception("Trying to split with wrong number of channels! Expected 3 channels, got ${channels()}")
  }
  val channels = split()
  return Pair3(channels[0], channels[1], channels[2])
}

fun Mat.splitArgb(): Pair4<Mat, Mat, Mat, Mat> {
  if (channels() != 4) {
    throw Exception("Trying to split with wrong number of channels! Expected 4 channels, got ${channels()}")
  }
  val channels = split()
  return Pair4(channels[0], channels[1], channels[2], channels[3])
}

fun List<Mat>.merge(format: ImageFormat): Mat {
  if (format.numChannels != size) {
    throw Exception("Trying to merge with incorrect number of channels. Format: ${format.name}, number of channels expected: ${format.numChannels}, number of channels received: $size")
  }

  return Mat(this[0].rows(), this[0].cols(), format.openCVFormat).also { newMat ->
    Core.merge(this, newMat)
  }
}

fun Mat.getByteArray(p: Point) = ByteArray(channels()).also { get(p.yi, p.xi, it) }

fun Mat.getValue(p: Point): Int = getFormat().toIntValue(getByteArray(p))

fun Mat.splitArray(): Array<Mat> = split().toTypedArray()

fun Mat.getByteArray(): ByteArray = ByteArray(width() * height() * channels())
  .also { get(0, 0, it) }

fun Mat.setChannels(vararg channels: Mat) {
  Core.merge(channels.toList(), this)
}

operator fun Mat.times(other: Double): Mat {
  val scalar = when (channels()) {
    1 -> Scalar(other)
    2 -> Scalar(other, other)
    3 -> Scalar(other, other, other)
    else -> Scalar(other, other, other, other)
  }

  return asBlankMat().also { newMat ->
    Core.multiply(this, scalar, newMat)
  }
}

fun List<Mat>.shuffle(currOrder: String, newOrder: String, newFormat: ImageFormat): Mat {
  if (!currOrder.isAllUniqueChars()) {
    throw Exception("currOrder mush include only unique chars. Received currOrder: $currOrder")
  } else if (currOrder.length != size) {
    throw Exception("Trying to shuffle the wrong number of channels. Expected length: ${size}, actual length: $currOrder")
  } else if (newOrder.length != newFormat.numChannels) {
    throw Exception("Output channels don't match between format and order string. newFormat channels: ${newFormat.numChannels}, new order channels: ${newOrder.length}")
  } else if (any { it.channels() != 1 }) {
    throw Exception("list provided needs to be all single channel mats.")
  } else if (!currOrder.toSet().containsAll(newOrder.toSet())) {
    throw Exception("unknown chars provided in newOrder. currOrder: $currOrder, newOrder: $newOrder")
  }

  val mapFromCharToChannel = currOrder
    .mapIndexed { index, c -> c to get(index) }
    .toMap()

  val newMatList: List<Mat> = newOrder.map { c -> mapFromCharToChannel[c]!! }

  return newMatList.merge(newFormat)
}

fun Mat.shuffle(currOrder: String, newOrder: String, newFormat: ImageFormat) =
  split().shuffle(currOrder, newOrder, newFormat)

fun Mat.crop(crop: BoundRect) = submat(crop)

fun Mat.scale(amount: Point) = resize(size * amount)

fun Mat.resize(newSize: Point) =
  if (newSize == size) this
  else Mat(newSize.yi, newSize.xi, getFormat().openCVFormat).apply {
    if (empty()) return@apply
    Imgproc.resize(this@resize, this, this.size())
  }

fun Mat.copy() = applyWithDest { src, dest -> src.copyTo(dest) }
