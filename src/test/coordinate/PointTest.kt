package test.coordinate

import coordinate.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PointTest {

  @Test
  fun testDist() {
    assertEquals(5f, Point.Zero.dist(Point(0, 5)))
    assertEquals(5f, Point.Zero.dist(Point(5, 0)))
    assertEquals(5f, Point.Zero.dist(Point(3, 4)))
    assertEquals(5f, Point.One.dist(Point(4, 5)))
    assertEquals(5f, Point.One.dist(Point(-2, -3)))
  }
}