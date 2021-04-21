package test.coordinate

import coordinate.BoundRect
import coordinate.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


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

  @Test
  fun testExpand() {
    val r = BoundRect(Point.Zero, 5.0, 5.0)
    assertEquals(
      BoundRect(Point(-1, -1), 7, 7),
      r.expand(1.0),
    )

    assertEquals(
      BoundRect(Point(1, 1), 3, 3),
      r.expand(-1.0),
    )

    assertEquals(
      BoundRect(Point(1, -1), 3, 7),
      r.expand(-1.0, 1),
    )

    assertEquals(
      BoundRect(Point(-2, -1), 9, 7),
      r.expand(2, 1),
    )

    assertEquals(
      BoundRect(Point(-2, -1), 9, 7),
      r.expand(Point(2, 1)),
    )
  }

  @Test
  fun testShrink() {
    val r = BoundRect(Point.Zero, 5.0, 5.0)
    assertEquals(
      BoundRect(Point(1, 1), 3, 3),
      r.shrink(1.0),
    )

    assertEquals(
      BoundRect(Point(-1, -1), 7, 7),
      r.shrink(-1.0),
    )

    assertEquals(
      BoundRect(Point(-1, 1), 7, 3),
      r.shrink(-1.0, 1),
    )

    assertEquals(
      BoundRect(Point(2, 1), 1, 3),
      r.shrink(2, 1),
    )

    assertEquals(
      BoundRect(Point(2, 1), 1, 3),
      r.shrink(Point(2, 1)),
    )
  }

  @Test
  fun testRecenter() {
    val r = BoundRect(Point.Zero, 5.0, 5.0)

    assertEquals(BoundRect(Point(-2.5, -2.5), 5, 5), r.recentered(Point.Zero))
    assertEquals(r, r.recentered(Point(2.5, 2.5)))


    val r2 = BoundRect(Point(-1, -1), 4.0, 4.0)

    assertEquals(BoundRect(Point(-2, -2), 4, 4), r2.recentered(Point.Zero))
    assertEquals(BoundRect(Point(148, 98), 4, 4), r2.recentered(Point(150, 100)))

    val r3 = BoundRect(Point(-1, -1), 4.0, 10.0)

    assertEquals(BoundRect(Point(-2, -5), 4, 10), r3.recentered(Point.Zero))
    assertEquals(BoundRect(Point(148, 95), 4, 10), r3.recentered(Point(150, 100)))
  }

  @Test
  fun testResizeCentered() {
    val r = BoundRect(Point.Zero, 5.0, 5.0)

    assertEquals(r, r.resizeCentered(Point(5, 5)))
    assertEquals(BoundRect(Point(-2.5, 0), 10, 5), r.resizeCentered(Point(10, 5)))
    assertEquals(BoundRect(Point(0, -2.5), 5, 10), r.resizeCentered(Point(5, 10)))
    assertEquals(BoundRect(Point(-2.5, -2.5), 10, 10), r.resizeCentered(Point(10, 10)))
    assertEquals(BoundRect(Point(-5, -2.5), 15, 10), r.resizeCentered(Point(15, 10)))
    assertEquals(BoundRect(Point(-5, 1), 15, 3), r.resizeCentered(Point(15, 3)))
    assertEquals(BoundRect(Point(2.5, 1), 0, 3), r.resizeCentered(Point(0, 3)))
  }
}
