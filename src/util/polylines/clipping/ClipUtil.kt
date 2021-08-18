package util.polylines.clipping

import de.lighti.clipper.Clipper
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.DIFFERENCE
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import de.lighti.clipper.Clipper.ClipType.UNION
import de.lighti.clipper.Clipper.ClipType.XOR
import de.lighti.clipper.Clipper.PolyType.CLIP
import de.lighti.clipper.Clipper.PolyType.SUBJECT
import de.lighti.clipper.DefaultClipper
import de.lighti.clipper.PolyTree
import util.polylines.PolyLine
import util.polylines.isClosed

fun DefaultClipper.addPath(
  line: PolyLine,
  polyType: Clipper.PolyType,
  forceClosed: Boolean? = null
) = addPath(line.toClipperPath(), polyType, forceClosed ?: line.isClosed())

fun DefaultClipper.addPaths(
  lines: List<PolyLine>,
  polyType: Clipper.PolyType,
  forceClosed: Boolean? = null
) = lines.map { line -> addPath(line, polyType, forceClosed) }

fun DefaultClipper.addClips(lines: List<PolyLine>) = addPaths(lines, CLIP, true)

fun DefaultClipper.addSubjects(lines: List<PolyLine>, forceClosed: Boolean? = null) =
  addPaths(lines, SUBJECT, forceClosed)

private fun List<PolyLine>.getClipper(
  clip: List<PolyLine>,
  forceClosed: Boolean? = null
): DefaultClipper = DefaultClipper().apply {
  addSubjects(this@getClipper, forceClosed)
  addClips(clip)
}

private fun List<PolyLine>.operationAsTree(
  operationType: ClipType,
  other: List<PolyLine>,
  forceClosed: Boolean? = null
) = PolyTree().also { getClipper(other, forceClosed).execute(operationType, it) }

private fun List<PolyLine>.operation(
  operationType: ClipType,
  other: List<PolyLine>,
  forceClosed: Boolean? = null
) = operationAsTree(operationType, other, forceClosed).toPolyLines(forceClosed)

fun List<PolyLine>.intersection(other: List<PolyLine>, forceClosed: Boolean? = null) =
  operation(INTERSECTION, other, forceClosed)

fun List<PolyLine>.diff(other: List<PolyLine>, forceClosed: Boolean? = null) =
  operation(DIFFERENCE, other, forceClosed)

fun List<PolyLine>.union(other: List<PolyLine>, forceClosed: Boolean? = null) =
  operation(UNION, other, forceClosed)

fun List<PolyLine>.xor(other: List<PolyLine>, forceClosed: Boolean? = null) =
  operation(XOR, other, forceClosed)
