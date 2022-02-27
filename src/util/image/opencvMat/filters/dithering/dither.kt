package util.image.opencvMat.filters.dithering

import org.opencv.core.CvType
import org.opencv.core.Mat
import util.image.ImageFormat.Gray
import util.image.converted
import util.image.opencvMat.applyWithDest
import util.image.opencvMat.toIntArray
import util.iterators.addOrNull
import util.iterators.forEach2D
import util.iterators.getOrNull

fun Mat.dither(type: DitherType, threshold: Number = 128, inPlace: Boolean = false) =
  converted(to = Gray).applyWithDest(inPlace = inPlace) { src, dest ->
    if (src.type() != CvType.CV_8UC1) {
      throw Exception("Dither only supports black and white images right now.")
    }

    val thresholdInt = threshold.toInt()

    val errorValues = Array(rows()) { DoubleArray(cols()) { 0.0 } }

    toIntArray(0).forEach2D { rowIndex, colIndex, pixelValue ->
      val errorValue = errorValues.getOrNull(rowIndex, colIndex) ?: 0.0
      val value = pixelValue.toDouble() + errorValue

      val errorVal = thresholdInt - value

      val newValue = if (value >= thresholdInt) 255.0 else 0.0

      dest.put(rowIndex, colIndex, newValue)

      val errorDivided = errorVal * type.divisor

      type.errorArray.forEach2D { errRowIndex, errColIndex, item ->
        val newColIndex = colIndex + errColIndex - type.errorColIndex
        val newRowIndex = rowIndex + errRowIndex - type.errorRowIndex

        errorValues.addOrNull(newRowIndex, newColIndex, item * errorDivided)
      }
    }
  }
