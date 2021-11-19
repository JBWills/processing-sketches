package util.polylines.clipping.simplify

import arrow.core.memoize
import util.iterators.listWrapped
import util.polylines.PolyLine
import util.polylines.clipping.ClipScale
import util.polylines.clipping.toClipperPath
import util.polylines.clipping.toPolyLines

fun PolyLine.toSimplePolygon(): List<PolyLine> =
  listWrapped().toSimplePolygons()


fun List<PolyLine>.toSimplePolygons(): List<PolyLine> = flatMap {
  it.toClipperPath(ClipScale).toSimplePolygon().toPolyLines(scale = 1 / ClipScale)
}

private val toSimplePolygonsMemoVal = List<PolyLine>::toSimplePolygons.memoize()

fun List<PolyLine>.toSimplePolygonsMemo() = toSimplePolygonsMemoVal(this)
