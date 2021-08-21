package util.polylines

fun PolyLine.countPoints(): Int = size

@JvmName("countPointsPolyLines")
fun List<PolyLine>.countPoints(): Int = sumBy { it.countPoints() }

@JvmName("countPointsPolyLinesList")
fun List<List<PolyLine>>.countPoints(): Int = sumBy { it.countPoints() }

@JvmName("countPointsPolyLinesListList")
fun List<List<List<PolyLine>>>.countPoints(): Int = sumBy { it.countPoints() }

fun PolyLine.countPolys(): Int = 1

@JvmName("countPolysPolyLines")
fun List<PolyLine>.countPolys(): Int = size

@JvmName("countPolysPolyLinesList")
fun List<List<PolyLine>>.countPolys(): Int = sumBy { it.countPolys() }

@JvmName("countPolysPolyLinesListList")
fun List<List<List<PolyLine>>>.countPolys(): Int = sumBy { it.countPolys() }
