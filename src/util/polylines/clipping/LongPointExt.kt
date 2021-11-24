package util.polylines.clipping

import coordinate.Point
import de.lighti.clipper.Point.LongPoint
import util.numbers.bound

// These values are from de.lighti.clipper.ClipperBase.java
private const val HighRange: Long = 0x3FFFFFFFFFFFFFFFL

fun Point.toLongPoint(scale: Double): LongPoint =
  LongPoint((x * scale).toLong(), (y * scale).toLong())

fun LongPoint.bound(maxValue: Long = HighRange) =
  LongPoint(
    x.bound(start = -maxValue, end = maxValue),
    y.bound(start = -maxValue, end = maxValue),
  )
