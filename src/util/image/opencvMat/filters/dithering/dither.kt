package util.image.opencvMat.filters.dithering

import org.opencv.core.CvType
import org.opencv.core.Mat
import util.image.opencvMat.applyWithDest
import util.image.opencvMat.toIntArray
import util.iterators.addOrNull
import util.iterators.forEach2D
import util.iterators.getOrNull

fun Mat.dither(type: DitherType, threshold: Number = 128, inPlace: Boolean = false) =
  applyWithDest(inPlace = inPlace) { src, dest ->
    if (src.type() != CvType.CV_8UC1) {
      throw Exception("Dither only supports black and white images right now.")
    }

    val thresholdInt = threshold.toInt()

    val errorValues = Array(rows()) { DoubleArray(cols()) { 0.0 } }

    toIntArray(0).forEach2D { rowIndex, colIndex, pixelValue ->
      val oldErrorValue = errorValues.getOrNull(rowIndex, colIndex) ?: 0.0
      val value = pixelValue.toDouble() + oldErrorValue

      val (newValue, errorValue) = if (value >= thresholdInt) 255.0 to value - 255.0 else 0.0 to value

      dest.put(rowIndex, colIndex, newValue)

      val errorDivided = errorValue * type.divisor

      type.errorArray.forEach2D { errRowIndex, errColIndex, item ->
        val newColIndex = colIndex + errColIndex - type.errorColIndex
        val newRowIndex = rowIndex + errRowIndex - type.errorRowIndex

        errorValues.addOrNull(newRowIndex, newColIndex, item * errorDivided)
      }
    }
  }
