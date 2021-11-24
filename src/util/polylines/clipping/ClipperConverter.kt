package util.polylines.clipping

import coordinate.Point
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import de.lighti.clipper.Point.LongPoint
import de.lighti.clipper.PolyTree
import util.base.doIf
import util.polylines.PolyLine
import util.polylines.closed

/*
 * Convenience file for converting between my objects and de.lighti.clipper objects
 */

fun PolyLine.toLongPointList(scale: Double): List<LongPoint> = map { it.toLongPoint(scale) }

fun PolyLine.toClipperPath(scale: Double): Path =
  Path(size).also { path -> path.addAll(run { toLongPointList(scale) }) }

@JvmName("lineToClipperPaths")
fun PolyLine.toClipperPaths(scale: Double): Paths = listOf(this).toClipperPaths(scale)

@JvmName("lineListToClipperPaths")
fun List<PolyLine>.toClipperPaths(scale: Double = 1.0): Paths =
  Paths(size).also { path -> path.addAll(this.map { line -> line.toClipperPath(scale) }) }

fun Path.toClipperPaths(): Paths = Paths(1).also { it.add(this) }

fun LongPoint.toPoint(): Point = Point(x, y)

fun Path.toPolyLine(closed: Boolean = false, scale: Double): PolyLine =
  map { it.toPoint() * scale }
    .doIf(closed) { it.closed() }

fun Paths.toPolyLines(closed: Boolean = false, scale: Double): List<PolyLine> =
  map { it.toPolyLine(closed, scale) }

fun PolyTree.toPolyLines(forceClose: Boolean? = null, scale: Double = 1.0): List<PolyLine> =
  Paths.openPathsFromPolyTree(this).toPolyLines(closed = forceClose ?: false, scale) +
    Paths.closedPathsFromPolyTree(this).toPolyLines(closed = forceClose ?: true, scale)
