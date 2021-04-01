package test.coordinate

import coordinate.BoundRect
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.sqrt
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class RectTest {
  /*
   * Line Segment tests
   */
  @Test
  fun testGetBoundSegment() {
    val r = BoundRect(Point(0, -1), 5.0, 5.0)
    assertEquals(S(Point.Zero, Point(5, 0)), r.getBoundSegment(S(Point.Zero, Point(6, 0))))
    assertEquals(S(Point.Zero, Point(5, 0)), r.getBoundSegment(S(Point(-1, 0), Point(6, 0))))
    assertEquals(S(Point(0, -1), Point(5, -1)), r.getBoundSegment(S(Point(-1, -1), Point(6, -1))))
  }
}