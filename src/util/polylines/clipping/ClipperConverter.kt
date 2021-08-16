package util.polylines.clipping

import coordinate.Point
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import de.lighti.clipper.Point.DoublePoint
import de.lighti.clipper.Point.LongPoint
import util.doIf
import util.polylines.PolyLine
import util.polylines.closed

/*
 * Convenience file for converting between my objects and de.lighti.clipper objects
 */

fun Point.toDoublePoint(): DoublePoint = DoublePoint(x, y)
fun Point.toLongPoint(): LongPoint = LongPoint(xl, yl)

fun PolyLine.toLongPointList(): List<LongPoint> = map { it.toLongPoint() }

fun PolyLine.toClipperPath(): Path = Path(size).also { path -> path.addAll(toLongPointList()) }

fun List<PolyLine>.toClipperPaths(): Paths =
  Paths(size).also { paths -> paths.addAll(map { it.toClipperPath() }) }


fun DoublePoint.toPoint(): Point = Point(x, y)
fun LongPoint.toPoint(): Point = Point(x, y)

fun Path.toPolyLine(closed: Boolean = false): PolyLine =
  map { it.toPoint() }
    .doIf(closed) { it.closed() }

fun Paths.toPolyLines(closed: Boolean = false): List<PolyLine> = map { it.toPolyLine(closed) }
