package interfaces.shape

import coordinate.Line
import coordinate.Point
import coordinate.Segment
import util.polylines.PolyLine

/**
 * Represents a closed shape (can be self intersecting).
 */
interface Maskable {
  fun contains(p: Point): Boolean

  fun intersection(line: Line, memoized: Boolean = false): List<Segment>
  fun intersection(segment: Segment, memoized: Boolean = false): List<Segment>
  fun diff(segment: Segment, memoized: Boolean = false): List<Segment>

  fun intersection(polyLine: PolyLine, memoized: Boolean = false): List<PolyLine>
  fun diff(polyLine: PolyLine, memoized: Boolean = false): List<PolyLine>

}
