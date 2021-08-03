package util.image.opencvMat

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.letWith
import util.map
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

fun Mat.get(p: Point, band: Int = 0): Double? = (p.x.toInt() to p.y.toInt()).let { (col, row) ->
  if (!(0 until cols()).contains(col)) return null
  if (!(0 until rows()).contains(row)) return null
  get(row, col)?.get(band)?.let { if (it.isNaN()) null else it }
}


fun Mat.getOr(p: Point, default: Double, band: Int = 0): Double = get(p, band) ?: default
fun createMat(rows: Int, cols: Int, format: ImageFormat, baseColor: Color) =
  Mat(rows, cols, format.openCVFormat, format.colorToScalar(baseColor))

fun BoundRect.toEmptyMat(type: ImageFormat) = Mat(heightPx, widthPx, type.openCVFormat)

fun ByteArray.toBuffer(): ByteBuffer = ByteBuffer.wrap(this)
fun ByteArray.asIntBuffer(): IntBuffer = toBuffer().asIntBuffer()

fun IntBuffer.copyTo(arr: IntArray): IntBuffer = get(arr)

fun Mat.asBlankMat(type: Int = type()) = Mat(rows(), cols(), type)

fun Mat.getByteArray(p: Point) = ByteArray(channels()).also { get(p.yi, p.xi, it) }

fun Mat.getValue(p: Point): Int = getFormat().toIntValue(getByteArray(p))

fun Mat.getByteArray(): ByteArray = ByteArray(width() * height() * channels())
  .also { get(0, 0, it) }

fun Mat.crop(crop: BoundRect) = submat(crop)

fun Mat.scale(amount: Point) = resize(size * amount)

fun Mat.resize(newSize: Point) =
  if (newSize == size) this
  else Mat(newSize.yi, newSize.xi, getFormat().openCVFormat).apply {
    if (empty()) return@apply
    Imgproc.resize(this@resize, this, this.size())
  }

fun Mat.copy() = applyWithDest { src, dest -> src.copyTo(dest) }
