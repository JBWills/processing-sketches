package util.polylines

import coordinate.Point
import util.map

fun PolyLine.toEmptyDoubleArray() = DoubleArray(size * 2)

fun PolyLine.toDoubleArray() = toEmptyDoubleArray().also { resultArr ->
  forEachIndexed { i, point ->
    resultArr[2 * i] = point.x
    resultArr[2 * i + 1] = point.y
  }
}

fun DoubleArray.toPolyLine(): PolyLine = (size / 2).map { i ->
  Point(this[2 * i], this[2 * i + 1])
}
