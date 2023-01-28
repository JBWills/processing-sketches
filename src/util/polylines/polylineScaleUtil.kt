package util.polylines

import coordinate.Point

fun PolyLine.scale(amt: Number) = map { it * amt }

fun PolyLine.scale(amt: Number, anchor: Point): PolyLine = map { p ->
  // Point = 1, 1
  // anchor = 2,2
  // scale = 0.5
  // expected = 1.5, 1.5
  val vec = anchor - p // 1, 1
  val scaledVec = vec * (1.0 - amt.toDouble()) // 0.5, 0.5
  p + scaledVec
}
