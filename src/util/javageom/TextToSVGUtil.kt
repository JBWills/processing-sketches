package util.javageom

import arrow.core.memoize
import util.debugLog
import util.iterators.skipLast
import util.javageom.pathiterator.PathSegmentType.Close
import util.javageom.pathiterator.PathSegmentType.CubicTo
import util.javageom.pathiterator.PathSegmentType.LineTo
import util.javageom.pathiterator.PathSegmentType.MoveTo
import util.javageom.pathiterator.PathSegmentType.QuadTo
import util.javageom.pathiterator.forEach
import util.polylines.MutablePolyLine
import util.polylines.PolyLine
import util.polylines.closedInPlace
import java.awt.Font
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform

const val DefaultFlatness = 20.0
val DefaultFontRenderContext = FontRenderContext(null, true, true)

/**
 * Convert a shape to a flattened PolyLine. "Flattened" means that we approximate all of the
 * bezier curves. TODO add a more accurate curvy version.
 *
 * @param transform transform to apply to the points. If null, no transform is applied
 * @param flatness (From PathIterator): the maximum distance that the line segments used to
 *   approximate the curved segments are allowed to deviate from any point on the original curve
 */
private fun Shape.toPolyLines(
  transform: AffineTransform? = null,
  flatness: Double = DefaultFlatness
): List<PolyLine> {
  debugLog("Transform: $transform")
  val lines: MutableList<MutablePolyLine> = mutableListOf()
  getPathIterator(transform, flatness)
    .forEach { (type, points) ->
      when (type) {
        MoveTo -> lines.add(points.toMutableList())
        LineTo -> lines.last().addAll(points)
        Close -> lines.last().closedInPlace()
        QuadTo -> throw Exception("Unexpected QuadTo in flattened shape")
        CubicTo -> throw Exception("Unexpected CubicTo in flattened shape")
      }
    }

  return lines.map { it.skipLast() }
}

private fun List<Shape>.toPolyLines(
  transforms: List<AffineTransform>?,
  flatness: Double
): List<List<PolyLine>> =
  mapIndexed { index, shape -> shape.toPolyLines(transforms?.getOrNull(index), flatness) }

private fun String.getLayoutAndOutlines(font: Font): List<Pair<TextLayout, Shape>> =
  split("\\n")
    .map { line ->
      debugLog(line)
      val t = TextLayout(line, font, DefaultFontRenderContext)

      t to t.getOutline(null)
    }

val getLayoutAndOutlinesMemo = String::getLayoutAndOutlines.memoize()

fun String.toPolyLine(
  font: Font,
  flatness: Double = DefaultFlatness,
  getTransforms: ((List<Pair<TextLayout, Shape>>) -> List<AffineTransform>)? = null,
): List<List<PolyLine>> =
  getLayoutAndOutlinesMemo(this, font.deriveFont(10f))
    .let { layoutsAndOutlines ->
      layoutsAndOutlines
        .map { it.second }
        .toPolyLines(getTransforms?.invoke(layoutsAndOutlines), flatness = flatness)
    }
