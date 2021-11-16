@file:Suppress("EmptyRange")

package tests.util

import coordinate.Point
import org.junit.jupiter.api.Test
import util.atAmountAlong
import util.base.numSteps
import util.base.step
import util.percentAlong
import kotlin.test.assertEquals

internal class RangeTest {

  @Test
  fun testIterator() {
    assertEquals(
      listOf(0.0, 1.0, 2.0),
      (0.0..2.0 step 1.0).map { it },
    )
    assertEquals(
      listOf(2.0, 1.0, 0.0),
      (2.0..0.0 step -1.0).map { it },
    )
    assertEquals(
      listOf(0.0, 2.0),
      (0.0..2.0 step 2.0).map { it },
    )
    assertEquals(
      listOf(0.0),
      (0.0..0.0 step 1.0).map { it },
    )
  }

  @Test
  fun testPointRange() {
    assertEquals(
      listOf(Point.Zero, Point(0, 0.5), Point(0, 1)),
      (Point.Zero..Point(0, 1) step 0.5).toList(),
    )
    assertEquals(
      listOf(Point.Zero, Point(0, 0.6), Point(0, 1)),
      (Point.Zero..Point(0, 1) step 0.6).toList(),
    )
    assertEquals(listOf(Point.Zero, Point(0, 1)), (Point.Zero..Point(0, 1) step 2.0).toList())
    assertEquals(
      listOf(Point(0, 1), Point(0, 0.5), Point.Zero),
      (Point.Zero..Point(0, 1) step -0.5).toList(),
    )
  }

  @Test
  fun testAt() {
    assertEquals(0.5, (0.0..1.0).atAmountAlong(0.5))
    assertEquals(0.0, (0.0..1.0).atAmountAlong(0.0))
    assertEquals(1.0, (0.0..1.0).atAmountAlong(1.0))
    assertEquals(0.0, (-1.0..1.0).atAmountAlong(0.5))
  }

  @Test
  fun testDoublePercentAlong() {
    assertEquals(0.5, (0.0..1.0).percentAlong(0.5))
    assertEquals(0.0, (0.0..1.0).percentAlong(0.0))
    assertEquals(1.0, (0.0..1.0).percentAlong(1.0))
    assertEquals(0.75, (-1.0..1.0).percentAlong(0.5))
    assertEquals(0.0, (-1.0..1.0).percentAlong(-1.0))
    assertEquals(-0.5, (0.0..-1.0).percentAlong(0.5))
  }

  @Test
  fun testIntPercentAlong() {
    assertEquals(0.5, (0..1).percentAlong(0.5))
    assertEquals(0.0, (0..1).percentAlong(0))
    assertEquals(1.0, (0..1).percentAlong(1))
    assertEquals(0.75, (-1..1).percentAlong(0.5))
    assertEquals(0.0, (-1..1).percentAlong(-1.0))
    assertEquals(-0.5, (0..-1).percentAlong(0.5))
  }

  @Test
  fun testNumSteps() {
    assertEquals(listOf(1.0, 1.25, 1.5, 1.75, 2.0), (1.0..2.0 numSteps 5.0).toList())
  }
}
