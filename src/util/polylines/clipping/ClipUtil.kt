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
fun DefaultClipper.addClips(lines: List<PolyLine>) = addPaths(lines, CLIP, true)

fun DefaultClipper.addSubjects(lines: List<PolyLine>, forceClosedValue: Boolean? = null) =
  addPaths(lines, SUBJECT, forceClosedValue)

private fun List<PolyLine>.getClipper(
  clip: List<PolyLine>,
  forceClosedValue: Boolean? = null
): DefaultClipper = DefaultClipper()
  .apply {
    addSubjects(this@getClipper, forceClosedValue)
    addClips(clip)
  }

private fun List<PolyLine>.operationAsTree(
  operationType: ClipType,
  other: List<PolyLine>,
  forceClosedValue: Boolean? = null
) =
  PolyTree().also { getClipper(other, forceClosedValue).execute(operationType, it) }

private fun List<PolyLine>.operation(
  operationType: ClipType,
  other: List<PolyLine>,
  forceClosedValue: Boolean? = null
) =
  operationAsTree(operationType, other, forceClosedValue).toPolyLines()

fun List<PolyLine>.clipperIntersection(other: PolyLine, forceClosedValue: Boolean? = null) =
  operation(INTERSECTION, listOf(other), forceClosedValue)

fun List<PolyLine>.clipperDiff(other: PolyLine, forceClosedValue: Boolean? = null) =
  operation(DIFFERENCE, listOf(other), forceClosedValue)

fun List<PolyLine>.clipperUnion(other: PolyLine, forceClosedValue: Boolean? = null) =
  operation(UNION, listOf(other), forceClosedValue)

fun List<PolyLine>.clipperXor(other: PolyLine, forceClosedValue: Boolean? = null) =
  operation(XOR, listOf(other), forceClosedValue)

@JvmName("clipperIntersectionMulti")
fun List<PolyLine>.clipperIntersection(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  operation(INTERSECTION, other, forceClosedValue)

@JvmName("clipperDiffMulti")
fun List<PolyLine>.clipperDiff(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  operation(DIFFERENCE, other, forceClosedValue)

@JvmName("clipperUnionMulti")
fun List<PolyLine>.clipperUnion(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  operation(UNION, other, forceClosedValue)

@JvmName("clipperXorMulti")
fun List<PolyLine>.clipperXor(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  operation(XOR, other, forceClosedValue)
