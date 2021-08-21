package util.polylines.clipping

import coordinate.Segment
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import util.polylines.PolyLine
import util.polylines.clipping.ClipperPaths.Companion.asPaths
import util.polylines.clipping.ForceClosedOption.Default
import util.polylines.toSegment

fun List<PolyLine>.clip(
  other: List<PolyLine>,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = asPaths(forceClosed).clip(op, other.asPaths())

@JvmName("operationPolyLineListToPolyLine")
fun List<PolyLine>.clip(
  other: PolyLine,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = asPaths(forceClosed).clip(op, other.asPaths())

@JvmName("operationPolyLineToPolyLineList")
fun PolyLine.clip(
  other: List<PolyLine>,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = asPaths(forceClosed).clip(op, other.asPaths())

@JvmName("operationPolyLineToPolyLine")
fun PolyLine.clip(other: PolyLine, op: ClipType, forceClosed: ForceClosedOption = Default) =
  asPaths(forceClosed).clip(op, other.asPaths())

fun Path.clip(other: Paths, op: ClipType, forceClosed: ForceClosedOption = Default) =
  asPaths(forceClosed).clip(op, other.asPaths())

fun Paths.clip(other: Path, op: ClipType, forceClosed: ForceClosedOption = Default) =
  asPaths(forceClosed).clip(op, other.asPaths())

fun Path.clip(other: Path, op: ClipType, forceClosed: ForceClosedOption = Default) =
  asPaths(forceClosed).clip(op, other.asPaths())

fun List<Segment>.clip(
  other: ClipperPaths,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
): List<Segment> = asPaths(forceClosed).clip(op, other.asPaths()).map { it.toSegment() }
