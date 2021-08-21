package coordinate.util

import coordinate.Deg
import kotlin.math.PI

fun Deg.arcLength(radius: Double) = (2 * PI * radius) * (value / 360.0)
