package util.io.geoJson

import org.geotools.geometry.jts.JTS.smooth
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import org.locationtech.jts.simplify.TopologyPreservingSimplifier
import org.locationtech.jts.simplify.VWSimplifier
import util.pointsAndLines.polyLine.PolyLine

typealias SimplifyFn = (geom: Geometry, amount: Double) -> Geometry

enum class SimplifyType(val simplify: SimplifyFn) {
  DouglasPeucker(DouglasPeuckerSimplifier::simplify),
  VW(VWSimplifier::simplify),
  TopologyPreserving(TopologyPreservingSimplifier::simplify),
}

fun Geometry.simplify(amount: Double, type: SimplifyType) = type.simplify(this, amount)
fun Geometry.smooth(amount: Double) = smooth(this, amount)

fun Geometry.asLineString(): LineString = this as LineString
fun List<Geometry>.asLineStrings(): List<LineString> = map { it as LineString }
fun List<LineString>.asPolyLines(): List<PolyLine> = map { it.toPolyLine() }

@JvmName("asPolyLinesGeometry")
fun List<Geometry>.asPolyLines(): List<PolyLine> = map { (it as LineString).toPolyLine() }
