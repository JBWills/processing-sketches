package util.polylines.clipping

import coordinate.Segment
import coordinate.Segment.Companion.segmentsToPolyLines
import de.lighti.clipper.Clipper.ClipType
import util.polylines.PolyLine
import util.polylines.clipping.ClipperPaths.Companion.asPaths
import util.polylines.clipping.ForceClosedOption.Default
import util.polylines.toSegment

const val ClipScale: Double = 1_000.0

fun List<PolyLine>.clip(
  other: List<PolyLine>,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = asPaths(forceClosed, ClipScale)
  .clip(op, other.asPaths(scale = ClipScale), 1 / ClipScale)

@JvmName("operationPolyLineListToPolyLine")
fun List<PolyLine>.clip(
  other: PolyLine,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = clip(listOf(other), op, forceClosed)

@JvmName("operationPolyLineToPolyLineList")
fun PolyLine.clip(
  other: List<PolyLine>,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
) = listOf(this).clip(other, op, forceClosed)

@JvmName("operationPolyLineToPolyLine")
fun PolyLine.clip(other: PolyLine, op: ClipType, forceClosed: ForceClosedOption = Default) =
  listOf(this).clip(listOf(other), op, forceClosed)

@JvmName("clipSegmentsAndLines")
fun List<Segment>.clip(
  other: List<PolyLine>,
  op: ClipType,
  forceClosed: ForceClosedOption = Default
): List<Segment> =
  segmentsToPolyLines()
    .clip(other, op, forceClosed)
    .map(PolyLine::toSegment)
