package util.image.opencvMat.filters.dithering

import org.opencv.core.Mat
import util.base.deltaE2000
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.opencvMat.applyWithDest
import util.image.opencvMat.toColorArray
import util.iterators.addOrNull
import util.iterators.forEach2D
import util.iterators.getOrNull
import util.numbers.bound
import java.awt.Color

fun Mat.dither(
  type: DitherType,
  format: ImageFormat = getFormat(),
  threshold: Number = 128,
  inPlace: Boolean = false,
  getValue: (Color) -> Double
) =
  applyWithDest(inPlace = inPlace) { src, dest ->
    val thresholdInt = threshold.toInt()

    val errorValues = Array(rows()) { DoubleArray(cols()) { 0.0 } }

    src.toColorArray(format).forEach2D { rowIndex, colIndex, pixelValue ->
      val oldErrorValue = errorValues.getOrNull(rowIndex, colIndex) ?: 0.0
      val value = getValue(pixelValue) + oldErrorValue

      val (newValue, errorValue) = if (value >= thresholdInt) 255.0 to value - 255.0 else 0.0 to value

      if (dest.channels() == 1) {
        dest.put(rowIndex, colIndex, newValue)
      } else if (dest.channels() == 2) {
        dest.put(rowIndex, colIndex, newValue, newValue)
      } else if (dest.channels() == 3) {
        dest.put(rowIndex, colIndex, newValue, newValue, newValue)
      }

      val errorDivided = errorValue * type.divisor

      type.errorArray.forEach2D { errRowIndex, errColIndex, item ->
        val newColIndex = colIndex + errColIndex - type.errorColIndex
        val newRowIndex = rowIndex + errRowIndex - type.errorRowIndex

        errorValues.addOrNull(newRowIndex, newColIndex, item * errorDivided)
      }
    }
  }

fun Mat.ditherByColor(
  type: DitherType,
  color: Color,
  maxDiff: Number, // from 0 to 100
  format: ImageFormat = getFormat(),
  inPlace: Boolean = false,
): Mat {
  val boundMaxDiff = maxDiff.toDouble().bound(0.0, 100.0)
  return dither(type, format, inPlace = inPlace) { pixelColor ->
    val diff = color.deltaE2000(pixelColor)
    if (diff > boundMaxDiff) {
      return@dither 0.0
    }

    (255 / boundMaxDiff) * (boundMaxDiff - diff)
  }
}
