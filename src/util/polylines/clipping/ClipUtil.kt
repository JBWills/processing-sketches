package util.polylines.clipping

import de.lighti.clipper.Clipper
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.PolyType.CLIP
import de.lighti.clipper.Clipper.PolyType.SUBJECT
import de.lighti.clipper.DefaultClipper
import de.lighti.clipper.PolyTree

private fun DefaultClipper.addPaths(
  lines: ClipperPaths,
  polyType: Clipper.PolyType,
) = lines.paths.map { line -> addPath(line, polyType, lines.isClosed(line)) }

private fun DefaultClipper.addClips(lines: ClipperPaths) =
  addPaths(lines.paths, CLIP, true)

private fun DefaultClipper.addSubjects(lines: ClipperPaths) = addPaths(lines, SUBJECT)

private fun ClipperPaths.getClipper(
  clip: ClipperPaths,
): DefaultClipper = DefaultClipper().apply {
  addSubjects(this@getClipper)
  addClips(clip)
}

private fun ClipperPaths.operationAsTree(
  operationType: ClipType,
  other: ClipperPaths,
) = PolyTree().also { getClipper(other).execute(operationType, it) }

fun ClipperPaths.clip(
  operationType: ClipType,
  other: ClipperPaths,
  scaleResult: Double,
) = operationAsTree(operationType, other)
  .toPolyLines(scale = scaleResult)
