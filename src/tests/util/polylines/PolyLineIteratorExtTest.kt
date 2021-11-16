package tests.util.polylines

import coordinate.Point
import org.junit.jupiter.api.Test
import util.polylines.walk
import kotlin.test.assertEquals

internal class PolyLineIteratorExtTest {

  fun p(y: Number) = Point(0, y)

  fun line(vararg ys: Number) = ys.map { p(it) }

  @Test
  fun testWalk() {
    val step = 1.0
    val line = line(0, 3)

    assertEquals(line(0, 1, 2, 3), line.walk(step = step))
  }

  @Test
  fun testWalkWithOffStep() {
    val step = 2.0
    val line = line(0, 3)

    assertEquals(line(0, 2, 3), line.walk(step = step))
  }

  @Test
  fun testWalkWithNegativeStep() {
    val step = 1.0
    val line = listOf(p(0), p(3))

    assertEquals(line(3, 2, 1, 0), line.walk(step = -step))
  }

  @Test
  fun testWalkWithEmptyLine() {
    val step = 1.0
    val line = line()

    assertEquals(line(), line.walk(step = step))
  }

  @Test
  fun testWalkWithBlock() {
    val step = 1.0
    val line = line(0, 3)

    assertEquals(line(0, 2, 4, 6), line.walk(step = step) { it * 2 })
  }

  @Test
  fun testWalkWithMultiplePoints() {
    val step = 1.0
    val line = line(0, 0, 3, 5)

    assertEquals(line(0, 2, 4, 6, 8, 10), line.walk(step = step) { it * 2 })
  }

  @Test
  fun testWalkWithNewDirection() {
    val step = 1.5
    val line = line(0, 0, 2, -4.5)

    assertEquals(line(0, 1.5, 1, -0.5, -2, -3.5, -4.5), line.walk(step = step))
  }

  @Test
  fun testWalkSkipOverMany() {
    val step = 7.0
    val line = line(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    assertEquals(line(0, 7, 10), line.walk(step = step))
  }

  @Test
  fun testWalkSkipOverManyChangeDirs() =
    assertEquals(
      line(0, -1, -5),
      line(0, 1, 2, 3, 2, 1, 0, -5)
        .walk(step = 7.0),
    )

  @Test
  fun testWalkOnePoint() =
    assertEquals(
      line(3),
      line(3)
        .walk(step = 7.0),
    )

  @Test
  fun testWalkTwoPoints() =
    assertEquals(
      line(0, 3),
      line(0, 3)
        .walk(step = 10.0),
    )
}
