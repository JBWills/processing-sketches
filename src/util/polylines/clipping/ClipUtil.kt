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
  forceClosedValue: Boolean? = null
) =
  addPath(line.toClipperPath(), polyType, forceClosedValue ?: line.isClosed())

fun DefaultClipper.addPaths(
  lines: List<PolyLine>,
  polyType: Clipper.PolyType,
  forceClosedValue: Boolean? = null
) = lines.map { line -> addPath(line, polyType, forceClosedValue) }

fun DefaultClipper.addClip(line: PolyLine) = addPath(line, CLIP, true)

fun DefaultClipper.addSubjects(lines: List<PolyLine>, forceClosedValue: Boolean? = null) =
  addPaths(lines, SUBJECT, forceClosedValue)

private fun List<PolyLine>.getClipper(clip: PolyLine): DefaultClipper = DefaultClipper()
  .apply {
    addSubjects(this@getClipper)
    addClip(clip)
  }

private fun List<PolyLine>.operation(operationType: ClipType, other: PolyLine) =
  PolyTree()
    .also { getClipper(other).execute(operationType, it) }
    .toPolyLines()

fun List<PolyLine>.clipperIntersection(other: PolyLine) = operation(INTERSECTION, other)
fun List<PolyLine>.clipperDiff(other: PolyLine) = operation(DIFFERENCE, other)
fun List<PolyLine>.clipperUnion(other: PolyLine) = operation(UNION, other)
fun List<PolyLine>.clipperXor(other: PolyLine) = operation(XOR, other)
