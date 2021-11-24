package util.polylines

import coordinate.Point
import coordinate.Segment

@JvmName("ShiftSegments")
fun List<Segment>.translated(p: Point) = map { it + p }

@JvmName("translatedLine")
fun PolyLine.translated(p: Point): PolyLine = map { it + p }

fun List<PolyLine>.translated(p: Point): List<PolyLine> = map { it.translated(p) }

fun MutablePolyLine.translatedInPlace(p: Point): Unit = indices.forEach { this[it] += p }
