package tests.coordinate

import coordinate.BoundRect
import coordinate.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.pointsAndLines.polyLine.bounds

internal class PointTest {

  @Test
  fun testDist() {
    assertEquals(5.0, Point.Zero.dist(Point(0, 5)))
    assertEquals(5.0, Point.Zero.dist(Point(5, 0)))
    assertEquals(5.0, Point.Zero.dist(Point(3, 4)))
    assertEquals(5.0, Point.One.dist(Point(4, 5)))
    assertEquals(5.0, Point.One.dist(Point(-2, -3)))
  }

  @Test
  fun testBounds() {
    val p0 = Point.Zero
    val p1 = Point.One
    val p2 = Point(2)
    val p3 = Point(3)

    assertEquals(BoundRect(p0, p0), listOf(p0).bounds)
    assertEquals(BoundRect(Point(1, 2), Point(1, 2)), listOf(Point(1, 2)).bounds)

    assertEquals(BoundRect(p0, p1), listOf(p0, p1).bounds)
    assertEquals(BoundRect(p0, p2), listOf(p0, p1, p2).bounds)
    assertEquals(BoundRect(p0, p2), listOf(p2, p1, p0).bounds)
    assertEquals(BoundRect(p1, p3), listOf(p2, p1, p3).bounds)

    assertEquals(
      BoundRect(Point(-1, 2), Point(5, 7)),
      listOf(Point(0, 2), Point(5, 6), Point(-1, 7)).bounds,
    )
  }
}
