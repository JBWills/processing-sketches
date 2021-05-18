package tests.coordinate

import coordinate.Point
import coordinate.Segment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class SegmentTest {
  @Test
  fun testEquals() {
    assertEquals(
      Segment(Point(1.1, 1.1), Point(2.1, 2.1)),
      Segment(Point(1.1, 1.1), Point(2.1, 2.1)),
    )
    assertEquals(
      Segment(Point(1, 1), Point(2, 2)),
      Segment(Point(1, 1), Point(2, 2)),
    )
    assertEquals(
      Segment(p1 = Point(x = 374.95, y = 369.04), p2 = Point(x = 374.95, y = 369.04)),
      Segment(p1 = Point(x = 374.95, y = 369.04), p2 = Point(x = 374.95, y = 369.04)),
    )
  }

  @Test
  fun testGetPointAtPercent() {
    val vertLine = Segment(Point.Zero, Point(0, 1))
    val diagLine = Segment(Point.Zero, Point(-1, -1))
    assertEquals(
      Point.Zero,
      vertLine.getPointAtPercent(0.0),
    )
    assertEquals(
      Point(0, 0.5),
      vertLine.getPointAtPercent(0.5),
    )
    assertEquals(
      Point(0, -0.5),
      vertLine.getPointAtPercent(-0.5),
    )
    assertEquals(
      Point(0, 1.5),
      vertLine.getPointAtPercent(1.5),
    )
    assertEquals(
      Point.Zero,
      diagLine.getPointAtPercent(0.0),
    )
    assertEquals(
      Point(-0.5, -0.5),
      diagLine.getPointAtPercent(0.5),
    )
    assertEquals(
      Point(0.5, 0.5),
      diagLine.getPointAtPercent(-0.5),
    )
    assertEquals(
      Point(-1.5, -1.5),
      diagLine.getPointAtPercent(1.5),
    )
  }
}
