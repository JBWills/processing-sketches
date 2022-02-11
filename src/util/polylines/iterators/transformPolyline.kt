package util.polylines.iterators

import coordinate.transforms.ShapeTransform
import interfaces.shape.transform
import util.iterators.deepDeepMap
import util.iterators.deepMap
import util.polylines.PolyLine

fun PolyLine.transform(t: ShapeTransform): PolyLine = map { it.transform(t) }

@JvmName("transformPolyLineList")
fun List<PolyLine>.transform(t: ShapeTransform): List<PolyLine> =
  deepMap { it.transform(t) }

@JvmName("transformPolyLineListList")
fun List<List<PolyLine>>.transform(t: ShapeTransform): List<List<PolyLine>> =
  deepDeepMap { it.transform(t) }
