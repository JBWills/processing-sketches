package util.polylines.clipping

import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import de.lighti.clipper.Clipper.PolyType.CLIP
import de.lighti.clipper.Clipper.PolyType.SUBJECT
import de.lighti.clipper.DefaultClipper
import de.lighti.clipper.Paths
import util.alsoDebugLog
import util.polylines.PolyLine


fun List<PolyLine>.getClipper(): DefaultClipper = DefaultClipper()
  .apply {
    addPath(toClipperPaths().first(), SUBJECT, true)
    addPath(toClipperPaths().last(), CLIP, true)
  }

fun DefaultClipper.intersection() = Paths().also { execute(INTERSECTION, it) }.toPolyLines()

fun List<PolyLine>.clipperIntersection() = getClipper().intersection().alsoDebugLog()


