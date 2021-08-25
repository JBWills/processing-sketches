package util.javageom.pathiterator

import coordinate.Point
import java.awt.geom.PathIterator
import java.awt.geom.PathIterator.SEG_CLOSE
import java.awt.geom.PathIterator.SEG_CUBICTO
import java.awt.geom.PathIterator.SEG_LINETO
import java.awt.geom.PathIterator.SEG_MOVETO
import java.awt.geom.PathIterator.SEG_QUADTO

/**
 * Enum defining path segments. This is a wrapper around the types in [PathIterator]
 *
 * @property id the int id as defined in [PathIterator]
 * @property numPoints the number of points the [PathIterator.currentSegment] response returns.
 */
enum class PathSegmentType(val id: Int, private val numPoints: Int) {
  /** Starting location for subpath */
  MoveTo(SEG_MOVETO, 1),

  /** Specifies endpoint from most recent point */
  LineTo(SEG_LINETO, 1),

  /** Specifies a pair of points that define quadratic curve*/
  QuadTo(SEG_QUADTO, 2),

  /** 3 points that define cubic bezier curve */
  CubicTo(SEG_CUBICTO, 3),

  /**
   * The segment type constant that specifies that
   * the preceding subpath should be closed by appending a line segment
   * back to the point corresponding to the most recent [SEG_MOVETO].
   */
  Close(SEG_CLOSE, 0),
  ;

  fun getPoints(segmentResponse: DoubleArray): List<Point> =
    (0 until numPoints)
      .map { pointIndex ->
        val startIndex = 2 * pointIndex
        Point(segmentResponse[startIndex], segmentResponse[startIndex + 1])
      }

  companion object {
    fun Int.asPathSegmentType() = values().find { it.id == this }
      ?: throw Exception("Could not find segment. Invalid PathSegmentType id passed: $this")
  }
}
