package tests.coordinate

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

  @Test
  fun testBoundIntersection() {
    val rZero = BoundRect(Point.Zero, 5, 5)
    val rOne = BoundRect(Point.One, 4, 4)
    val rTwo = BoundRect(Point.One, 5, 5)
    val rThree = BoundRect(Point.One, 3, 3)
    val rFour = BoundRect(Point.One, 3, 10)
    val disconnected = BoundRect(Point(10, 10), 3, 3)
    val disconnectedX = BoundRect(Point(10, 1), 3, 3)
    val disconnectedY = BoundRect(Point(1, 10), 3, 3)

    assertEquals(rZero, rZero.boundsIntersection(rZero))

    assertEquals(rOne, rZero.boundsIntersection(rOne))
    assertEquals(rOne, rOne.boundsIntersection(rZero))

    assertEquals(rOne, rTwo.boundsIntersection(rZero))
    assertEquals(rOne, rZero.boundsIntersection(rTwo))

    assertEquals(rThree, rThree.boundsIntersection(rZero))
    assertEquals(rThree, rZero.boundsIntersection(rThree))

    assertEquals(BoundRect(Point.One, 3, 4), rFour.boundsIntersection(rZero))
    assertEquals(BoundRect(Point.One, 3, 4), rZero.boundsIntersection(rFour))

    assertEquals(null, disconnected.boundsIntersection(rZero))
    assertEquals(null, rZero.boundsIntersection(disconnected))
    assertEquals(null, disconnectedX.boundsIntersection(rZero))
    assertEquals(null, rZero.boundsIntersection(disconnectedX))
    assertEquals(null, disconnectedY.boundsIntersection(rZero))
    assertEquals(null, rZero.boundsIntersection(disconnectedY))
  }

  @Test
  fun testScaled() {
    assertEquals(
      BoundRect(Point.Zero, Point(4)),
      BoundRect(Point.Zero, Point.One).scaled(Point(4), Point.Zero),
    )
    assertEquals(
      BoundRect(Point(-3), Point.One),
      BoundRect(Point.Zero, Point.One).scaled(Point(4), Point.One),
    )
    assertEquals(
      BoundRect(Point(-1.5), Point(2.5)),
      BoundRect(Point.Zero, Point.One).scaled(Point(4), Point(0.5)),
    )
  }

  @Test
  fun testExpandToInclude() {
    val b0 = BoundRect(Point.Zero, Point.Zero)
    val b1 = BoundRect(Point.Zero, Point(10, 20))
    assertEquals(b0, b0.expandToInclude(Point.Zero))
    assertEquals(BoundRect(Point.Zero, Point(10, 20)), b0.expandToInclude(Point(10, 20)))
    assertEquals(BoundRect(Point(-1, 0), Point(0, 20)), b0.expandToInclude(Point(-1, 20)))
    assertEquals(b1, b1.expandToInclude(Point(10, 20)))
    assertEquals(BoundRect(Point.Zero, Point(11, 20)), b1.expandToInclude(Point(11, 20)))
    assertEquals(BoundRect(Point.Zero, Point(11, 21)), b1.expandToInclude(Point(11, 21)))
    assertEquals(BoundRect(Point.Zero, Point(10, 21)), b1.expandToInclude(Point(9, 21)))
    assertEquals(
      BoundRect(Point(0, -21), Point(10, 20)), b1.expandToInclude(
      Point(9, -21)
      ))
  }
}
