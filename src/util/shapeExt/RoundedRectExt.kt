package util.shapeExt

import coordinate.BoundRect
import coordinate.RoundedRect

fun BoundRect.toRoundedRect(r: Double, step: Double? = null) =
  step?.let { RoundedRect(this, r, it) } ?: RoundedRect(this, r)
