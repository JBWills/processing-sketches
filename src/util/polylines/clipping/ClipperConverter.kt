package util.polylines.clipping

import coordinate.Point
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import de.lighti.clipper.Point.DoublePoint
import de.lighti.clipper.Point.LongPoint
import de.lighti.clipper.PolyNode
import de.lighti.clipper.PolyTree
import util.doIf
import util.polylines.PolyLine
import util.polylines.closed

/*
 * Convenience file for converting between my objects and de.lighti.clipper objects
 */

fun Point.toDoublePoint(): DoublePoint = DoublePoint(x, y)
fun Point.toLongPoint(): LongPoint = LongPoint(xl, yl)

fun PolyLine.toLongPointList(): List<LongPoint> = map(Point::toLongPoint)

fun PolyLine.toClipperPath(): Path = Path(size).also { path -> path.addAll(toLongPointList()) }

@JvmName("lineToClipperPaths")
fun PolyLine.toClipperPaths(): Paths = listOf(this).toClipperPaths()

@JvmName("lineListToClipperPaths")
fun List<PolyLine>.toClipperPaths(): Paths =
  Paths(size).also { paths -> paths.addAll(map { it.toClipperPath() }) }

fun Path.toClipperPaths(): Paths = Paths(1).also { it.add(this) }

fun DoublePoint.toPoint(): Point = Point(x, y)
fun LongPoint.toPoint(): Point = Point(x, y)

fun Path.toPolyLine(closed: Boolean = false): PolyLine =
  map { it.toPoint() }
    .doIf(closed) { it.closed() }

fun Paths.toPolyLines(closed: Boolean = false): List<PolyLine> =
  map { it.toPolyLine(closed) }

fun PolyNode.toPolyLine(forceClose: Boolean? = null): PolyLine =
  polygon.toPolyLine(closed = forceClose ?: !isOpen)

fun PolyTree.toPolyLines(forceClose: Boolean? = null): List<PolyLine> =
  Paths.openPathsFromPolyTree(this).toPolyLines(closed = forceClose ?: false) +
    Paths.closedPathsFromPolyTree(this).toPolyLines(closed = forceClose ?: true)
