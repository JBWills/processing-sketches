package util.polylines.clipping

import coordinate.BoundRect
import coordinate.Segment
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import util.polylines.PolyLine
import util.polylines.clipping.ForceClosedOption.Default

/**
 * Just a simple class to essentially allow any feasible type to be converted to a Paths object.
 *
 * @property paths the Clipper Paths object
 * @property forceClosed true if the Paths should be considered "Closed". This option is only used
 *   when the paths are "subjects" in a clipping operation, because "clip" paths are always considered
 *   closed.
 */
class ClipperPaths(val paths: Paths, private val forceClosed: ForceClosedOption) {
  constructor(path: Path, forceClosed: ForceClosedOption = Default) : this(
    path.toClipperPaths(),
    forceClosed,
  )

  fun isClosed(p: Path) = forceClosed.forceClose ?: p.isClosed

  fun <B> map(block: (Path) -> B): List<B> = paths.map(block)

  companion object {
    fun PolyLine.asPaths(forceClosed: ForceClosedOption = Default) =
      ClipperPaths(toClipperPaths(), forceClosed)

    @JvmName("asPathsPolyLineList")
    fun List<PolyLine>.asPaths(forceClosed: ForceClosedOption = Default) =
      ClipperPaths(toClipperPaths(), forceClosed)

    fun Path.asPaths(forceClosed: ForceClosedOption = Default) =
      ClipperPaths(toClipperPaths(), forceClosed)

    fun Paths.asPaths(forceClosed: ForceClosedOption = Default) =
      ClipperPaths(this, forceClosed)

    @JvmName("asPathsSegments")
    fun List<Segment>.asPaths(forceClosed: ForceClosedOption = Default) =
      map { it.toPolyLine() }.asPaths(forceClosed)

    @JvmName("asPathsBoundRect")
    fun BoundRect.asPaths(forceClosed: ForceClosedOption = Default) =
      toPolyLine().asPaths(forceClosed)

    fun ClipperPaths.asPaths(forceClosed: ForceClosedOption? = null) =
      ClipperPaths(paths, forceClosed ?: this.forceClosed)
  }
}
