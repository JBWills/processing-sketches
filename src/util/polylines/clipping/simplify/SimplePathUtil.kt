package util.polylines.clipping.simplify

import de.lighti.clipper.DefaultClipper
import de.lighti.clipper.Path
import de.lighti.clipper.Paths

fun Paths.toSimplePolygons(): Paths = DefaultClipper.simplifyPolygons(this)
fun Path.toSimplePolygon(): Paths = DefaultClipper.simplifyPolygon(this)
