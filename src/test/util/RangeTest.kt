package test.util

import coordinate.Point
import org.junit.jupiter.api.Test
import util.at
import util.percentAlong
import kotlin.test.assertEquals

internal class RangeTest {
  @Test
  fun testPointRange() {
    assertEquals(listOf(Point.Zero, Point(0, 0.5), Point(0, 1)), (Point.Zero..Point(0, 1) step 0.5).toList())
    assertEquals(listOf(Point.Zero, Point(0, 0.6), Point(0, 1)), (Point.Zero..Point(0, 1) step 0.6).toList())
    assertEquals(listOf(Point.Zero, Point(0, 1)), (Point.Zero..Point(0, 1) step 2.0).toList())
    assertEquals(listOf(Point(0, 1), Point(0, 0.5), Point.Zero), (Point.Zero..Point(0, 1) step -0.5).toList())
  }

  @Test
  fun testAt() {
    assertEquals(0.5, (0.0..1.0).at(0.5))
    assertEquals(0.0, (0.0..1.0).at(0.0))
    assertEquals(1.0, (0.0..1.0).at(1.0))
    assertEquals(0.0, (-1.0..1.0).at(0.5))
  }

  @Test
  fun testPercentAlong() {
    assertEquals(0.5, (0.0..1.0).percentAlong(0.5))
    assertEquals(0.0, (0.0..1.0).percentAlong(0.0))
    assertEquals(1.0, (0.0..1.0).percentAlong(1.0))
    assertEquals(0.75, (-1.0..1.0).percentAlong(0.5))
    assertEquals(0.0, (-1.0..1.0).percentAlong(-1.0))
  }
}