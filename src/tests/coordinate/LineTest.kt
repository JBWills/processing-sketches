package test.coordinate

import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import coordinate.getSlope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.sqrt
import kotlin.test.assertFalse
import kotlin.test.assertTrue

typealias S = Segment

internal class LineTest {
  /*
   * Line Segment tests
   */
  @Test
  fun testToLine() {
    fun assertToLineEquals(pointFrom: Point = Point.Zero, pointTo: Point, expectedDeg: Number) {
      assertEquals(
        Line(pointFrom, Deg(expectedDeg)),
        S(pointFrom, pointTo).toLine()
      )
    }

    assertToLineEquals(pointTo = Point.Up, expectedDeg = 90)
    assertToLineEquals(pointTo = Point.Down, expectedDeg = 90)
    assertToLineEquals(pointTo = Point.Right, expectedDeg = 0)
    assertToLineEquals(pointFrom = Point.Right, pointTo = Point.Zero, expectedDeg = 0)
    assertToLineEquals(pointTo = Point.Left, expectedDeg = 0)
    assertToLineEquals(pointTo = Point.One, expectedDeg = 45)
    assertToLineEquals(pointTo = Point(-1, 1), expectedDeg = 315)
    assertToLineEquals(pointTo = -Point.One, expectedDeg = 45)
  }

  @Test
  fun testConstructor() {
    assertEquals(
      S(Point.Zero, Point.Up),
      S(Point.Zero, Deg(90), 1.0)
    )

    assertEquals(
      S(Point.Zero, Point.Down),
      S(Point.Zero, Deg(-90), 1.0)
    )
    assertEquals(
      S(Point.Zero, Point.Left),
      S(Point.Zero, Deg(180), 1.0)
    )
    assertEquals(
      S(Point.Zero, Point.Right),
      S(Point.Zero, Deg(0), 1.0)
    )
    assertEquals(
      S(Point.Zero, Point(sqrt(0.5), -sqrt(0.5))),
      S(Point.Zero, Deg(45), 1.0)
    )
  }

  @Test
  fun testContainsWithVerticalLine() {
    val line = S(Point.Zero, Point(0, 2))

    assertTrue(line.contains(Point.Zero))
    assertTrue(line.contains(Point(0, 1)))
    assertTrue(line.contains(Point(0, 2)))
    assertFalse(line.contains(Point(0, 2.1)))
    assertFalse(line.contains(Point(-0.1, 1)))
    assertFalse(line.contains(Point(-0.1, 0)))
    assertFalse(line.contains(Point(0.1, 0)))
    assertFalse(line.contains(Point(0, -0.1)))

    assertTrue(line.flip().contains(Point.Zero))
    assertTrue(line.flip().contains(Point(0, 1)))
    assertTrue(line.flip().contains(Point(0, 2)))
    assertFalse(line.flip().contains(Point(0, 2.1)))
    assertFalse(line.flip().contains(Point(-0.1, 1)))
    assertFalse(line.flip().contains(Point(-0.1, 0)))
    assertFalse(line.flip().contains(Point(0.1, 0)))
    assertFalse(line.flip().contains(Point(0, -0.1)))
  }

  @Test
  fun testContainsWithHorizontalLine() {
    val line = S(Point.Zero, Point(2, 0))

    assertTrue(line.contains(Point.Zero))
    assertTrue(line.contains(Point(1, 0)))
    assertTrue(line.contains(Point(2, 0)))
    assertFalse(line.contains(Point(2.1, 0)))
    assertFalse(line.contains(Point(1, -0.1)))
    assertFalse(line.contains(Point(0, -0.1)))
    assertFalse(line.contains(Point(0, 0.1)))
    assertFalse(line.contains(Point(-0.1, 0)))

    assertTrue(line.flip().contains(Point.Zero))
    assertTrue(line.flip().contains(Point(1, 0)))
    assertTrue(line.flip().contains(Point(2, 0)))
    assertFalse(line.flip().contains(Point(2.1, 0)))
    assertFalse(line.flip().contains(Point(1, -0.1)))
    assertFalse(line.flip().contains(Point(0, -0.1)))
    assertFalse(line.flip().contains(Point(0, 0.1)))
    assertFalse(line.flip().contains(Point(-0.1, 0)))
  }

  @Test
  fun testContainsWithDiagonalLine() {
    val line = S(Point.Zero, Point(2, 2))

    assertTrue(line.contains(Point.Zero))
    assertTrue(line.contains(Point.One))
    assertTrue(line.contains(Point(2, 2)))
    assertFalse(line.contains(Point(2.1, 2.1)))
    assertFalse(line.contains(Point(-0.1, -0.1)))
    assertFalse(line.contains(Point(0, -0.1)))

    assertTrue(line.flip().contains(Point.Zero))
    assertTrue(line.flip().contains(Point.One))
    assertTrue(line.flip().contains(Point(2, 2)))
    assertFalse(line.flip().contains(Point(2.1, 2.1)))
    assertFalse(line.flip().contains(Point(-0.1, -0.1)))
    assertFalse(line.flip().contains(Point(0, -0.1)))
  }

  @Test
  fun testGetOverlap() {
    val a = Point.One
    val b = Point(2, 2)
    val c = Point(3, 3)
    val d = Point(4, 4)

    fun test(expected: S, s1: S, s2: S) = assertEquals(expected, s1.getOverlapWith(s2))

    test(expected = S(a, b), S(a, c), S(a, b))
    test(expected = S(a, b), S(a, b), S(a, c))
    test(expected = S(a, b), S(c, a), S(a, b))
    test(expected = S(b, a), S(b, a), S(a, c))
    test(expected = S(b, a), S(a, c), S(b, a))
    test(expected = S(a, b), S(a, b), S(c, a))
    test(expected = S(b, a), S(c, a), S(b, a))
    test(expected = S(b, a), S(b, a), S(c, a))

    test(expected = S(a, b), S(a, d), S(a, b))
    test(expected = S(b, c), S(a, d), S(b, c))
    test(expected = S(b, c), S(b, c), S(b, c))
    test(expected = S(c, b), S(c, b), S(c, b))

    test(expected = S(c, d), S(a, d), S(c, d))
    test(expected = S(a, d), S(a, d), S(d, a))
  }

  @Test
  fun testGetOverlapWithNoOverlap() {
    val a = Point.One
    val b = Point(2, 2)
    val c = Point(3, 3)
    val d = Point(4, 4)
    val e = Point(5, 5)

    fun test(expected: S?, s1: S, s2: S) = assertEquals(expected, s1.getOverlapWith(s2))

    test(expected = null, S(a, b), S(d, e))
    test(expected = null, S(a, b), S(b, e))
    test(expected = null, S(a, b), S(b, b))
    test(expected = null, S(a, c), S(b, b))
    test(expected = null, S(a, c), S(d, d))
  }

  @Test
  fun testIntersectWithLine() {
    val l = Line(Point.Zero, Deg(0))

    assertEquals(Point(1, 0), l.intersection(Line(Point.One, Deg(90))))
    assertEquals(Point(-1, 0), l.intersection(Line(-Point.One, Deg(90))))
    assertEquals(Point(0, 0), l.intersection(Line(Point(1, -1), Deg(45))))
  }

  @Test
  fun testGetSlope() {
    assertEquals(Deg(315), getSlope(Point.Zero, Point.One))
    assertEquals(Deg(0), getSlope(Point.Zero, Point.Right))
    assertEquals(Deg(180), getSlope(Point.Zero, Point.Left))
    assertEquals(Deg(90), getSlope(Point.Zero, Point.Up))
    assertEquals(Deg(270), getSlope(Point.Zero, Point.Down))
    assertEquals(Deg(135), getSlope(Point.Zero, -Point.One))
  }
}
