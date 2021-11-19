package util.polylines

fun PolyLine.scale(amt: Number) = map { it * amt }

fun List<PolyLine>.scaleAll(amt: Number): List<PolyLine> = map { it.scale(amt) }
