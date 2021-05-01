package util.pointsAndLines.polyLine

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.ContinuousMaskedShape
import coordinate.Point
import coordinate.Segment
import util.pointsAndLines.mutablePolyLine.MutablePolyLine

private val _bound = { list: PolyLine, bound: BoundRect ->
  ContinuousMaskedShape(list, bound).toBoundPoints(true)
}.memoize()

private val _translatedSegments = { list: List<Segment>, p: Point ->
  list.map { it + p }
}.memoize()

private val _translated = { list: List<Point>, p: Point ->
  list.map { it + p }
}.memoize()

fun PolyLine.bound(bound: BoundRect): List<PolyLine> = _bound(this, bound)

@JvmName("boundLines")
fun List<PolyLine>.bound(bound: BoundRect): List<PolyLine> = flatMap { _bound(it, bound) }

@JvmName("ShiftSegments")
fun List<Segment>.translated(p: Point) = _translatedSegments(this, p)

fun PolyLine.translated(p: Point): PolyLine = _translated(this, p)

fun MutablePolyLine.translatedInPlace(p: Point): Unit = indices.forEach { this[it] += p }
