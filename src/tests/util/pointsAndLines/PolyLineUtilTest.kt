package tests.util.pointsAndLines

import coordinate.Point
import org.junit.Test
import util.pointsAndLines.polyLine.normalizeDistances
import kotlin.test.assertEquals

internal class PolyLineUtilTest {
  @Test
  fun testNormalizeDistances() {
    assertEquals(listOf(), listOf<Point>().normalizeDistances(1.0..2.0))
    assertEquals(
      listOf(Point(0, 0), Point(0, 1)),
      listOf(Point(0, 0), Point(0, 1)).normalizeDistances(1.0..2.0),
    )
    assertEquals(
      listOf(Point(0, 0), Point(0, 1)),
      listOf(Point(0, 0), Point(0, 1)).normalizeDistances(0.0..1.0),
    )
    assertEquals(
      listOf(Point(0, 0), Point(0, 1)),
      listOf(Point(0, 0), Point(0, 1)).normalizeDistances(0.0..2.0),
    )
    assertEquals(
      listOf(Point(0, 0), Point(0, 1), Point(0, 3), Point(0, 4)),
      listOf(Point(0, 0), Point(0, 1), Point(0, 4)).normalizeDistances(0.0..2.0),
    )
    assertEquals(
      listOf(Point(0, 0), Point(0, 1), Point(0, 3), Point(0, 4)),
      listOf(Point(0, 0), Point(0, 0.5), Point(0, 1), Point(0, 4)).normalizeDistances(1.0..2.0),
    )
    assertEquals(
      listOf(Point(0, 0), Point(0, 2), Point(0, 4)),
      listOf(
        Point(0, 0),
        Point(0, 0.25),
        Point(0, 0.5),
        Point(0, 0.75),
        Point(0, 4),
      ).normalizeDistances(1.0..2.0),
    )

    assertEquals(
      listOf(Point(0, 0), Point(0, 2), Point(0, 3.75), Point(0, 4.0)),
      listOf(
        Point(0, 0),
        Point(0, 0.25),
        Point(0, 0.5),
        Point(0, 0.75),
        Point(0, 3.75),
        Point(0, 4),
      ).normalizeDistances(1.0..2.0),
    )
  }

  @Test
  fun testNormalizeDistancesCyclical() {
    assertEquals(
      listOf(Point(0, 0), Point(0, 1), Point(0, 0)),
      listOf(Point(0, 0), Point(0, 1), Point(0, 0)).normalizeDistances(1.0..2.0),
    )

    assertEquals(
      listOf(
        Point(0, 0),
        Point(0, 2),
        Point(0, 3.75),
        Point(0, 4.0),
        Point(0, 2.0),
        Point(0, 0.5),
        Point(0, 0),
      ),
      listOf(
        Point(0, 0),
        Point(0, 0.25),
        Point(0, 0.5),
        Point(0, 0.75),
        Point(0, 3.75),
        Point(0, 4),
        Point(0, 0.5),
        Point(0, 0),
      ).normalizeDistances(1.0..2.0),
    )
  }
}
