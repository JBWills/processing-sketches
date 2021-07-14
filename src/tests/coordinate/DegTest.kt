package tests.coordinate

import coordinate.Deg
import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.CounterClockwise
import coordinate.RotationDirection.EitherDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DegTest {

  @Test
  fun testConstructor() {
    assertEquals(0.0, Deg(0).value)
    assertEquals(90.0, Deg(90).value)
    assertEquals(350.0, Deg(-10).value)
    assertEquals(180.0, Deg(-180).value)
    assertEquals(0.0, Deg(360).value)
    assertEquals(0.0, Deg(-360).value)
    assertEquals(359.0, Deg(-361).value)
    assertEquals(1.0, Deg(361).value)
    assertEquals(180.0, Deg(-900).value)
  }

  @Test
  fun testPlus() {
    assertEquals(0.0, (Deg(90) + Deg(-90)).value)
    assertEquals(0.0, (Deg(0) + Deg(0)).value)
    assertEquals(10.0, (Deg(10) + Deg(0)).value)
    assertEquals(10.0, (Deg(0) + Deg(10)).value)
    assertEquals(20.0, (Deg(10) + Deg(10)).value)
    assertEquals(180.0, (Deg(300) + Deg(600)).value)
  }

  @Test
  fun testMinus() {
    assertEquals(180.0, (Deg(90) - Deg(-90)).value)
    assertEquals(0.0, (Deg(0) - Deg(0)).value)
    assertEquals(10.0, (Deg(10) - Deg(0)).value)
    assertEquals(350.0, (Deg(0) - Deg(10)).value)
    assertEquals(0.0, (Deg(10) - Deg(10)).value)
    assertEquals(60.0, (Deg(300) - Deg(600)).value)
  }

  @Test
  fun testRotation() {
    assertEquals(10.0, Deg(90).rotation(Deg(80)))
    assertEquals(90.0, Deg(90).rotation(Deg(360)))
    assertEquals(0.0, Deg(0).rotation(Deg(0)))
    assertEquals(1.0, Deg(0).rotation(Deg(359)))
    assertEquals(45.0, Deg(315).rotation(Deg(270)))
    assertEquals(135.0, Deg(315).rotation(Deg(90)))
    assertEquals(45.0, Deg(270).rotation(Deg(315)))
    assertEquals(135.0, Deg(90).rotation(Deg(315)))
  }

  @Test
  fun testRotationEitherDirection() {
    assertEquals(10.0, Deg(90).rotation(Deg(80), EitherDirection))
    assertEquals(90.0, Deg(90).rotation(Deg(360), EitherDirection))
    assertEquals(0.0, Deg(0).rotation(Deg(0), EitherDirection))
    assertEquals(1.0, Deg(0).rotation(Deg(359), EitherDirection))
    assertEquals(45.0, Deg(315).rotation(Deg(270), EitherDirection))
    assertEquals(135.0, Deg(315).rotation(Deg(90), EitherDirection))
    assertEquals(45.0, Deg(270).rotation(Deg(315), EitherDirection))
    assertEquals(135.0, Deg(90).rotation(Deg(315), EitherDirection))
  }

  @Test
  fun testRotationClockwise() {
    assertEquals(350.0, Deg(90).rotation(Deg(80), Clockwise))
    assertEquals(270.0, Deg(90).rotation(Deg(360), Clockwise))
    assertEquals(0.0, Deg(0).rotation(Deg(0), Clockwise))
    assertEquals(359.0, Deg(0).rotation(Deg(359), Clockwise))
    assertEquals(315.0, Deg(315).rotation(Deg(270), Clockwise))
    assertEquals(135.0, Deg(315).rotation(Deg(90), Clockwise))
    assertEquals(45.0, Deg(270).rotation(Deg(315), Clockwise))
    assertEquals(225.0, Deg(90).rotation(Deg(315), Clockwise))
  }

  @Test
  fun testRotationCounterClockwise() {
    assertEquals(10.0, Deg(90).rotation(Deg(80), CounterClockwise))
    assertEquals(90.0, Deg(90).rotation(Deg(360), CounterClockwise))
    assertEquals(0.0, Deg(0).rotation(Deg(0), CounterClockwise))
    assertEquals(1.0, Deg(0).rotation(Deg(359), CounterClockwise))
    assertEquals(45.0, Deg(315).rotation(Deg(270), CounterClockwise))
    assertEquals(225.0, Deg(315).rotation(Deg(90), CounterClockwise))
    assertEquals(315.0, Deg(270).rotation(Deg(315), CounterClockwise))
    assertEquals(135.0, Deg(90).rotation(Deg(315), CounterClockwise))
  }

  @Test
  fun testIsHorizontal() {
    assertEquals(false, Deg(90).isHorizontal())
    assertEquals(false, Deg(270).isHorizontal())
    assertEquals(false, Deg(45).isHorizontal())
    assertEquals(false, Deg(1).isHorizontal())
    assertEquals(true, Deg(0).isHorizontal())
    assertEquals(true, Deg(180).isHorizontal())
    assertEquals(true, Deg(360).isHorizontal())
  }

  @Test
  fun testIsVertical() {
    assertEquals(true, Deg(90).isVertical())
    assertEquals(true, Deg(270).isVertical())
    assertEquals(false, Deg(45).isVertical())
    assertEquals(false, Deg(1).isVertical())
    assertEquals(false, Deg(0).isVertical())
    assertEquals(false, Deg(180).isVertical())
    assertEquals(false, Deg(360).isVertical())
  }

  @Test
  fun isParallelWith() {
    fun t(expected: Boolean, d1: Number, d2: Number) {
      assertEquals(
        expected,
        Deg(d1).isParallelWith(d2),
        "expected Deg($d1).isParallelWith($d2) to be $expected",
      )
      assertEquals(
        expected,
        Deg(d1).isParallelWith(Deg(d2)),
        "expected Deg($d1).isParallelWith(Deg($d2)) to be $expected",
      )
      assertEquals(
        expected,
        Deg(d2).isParallelWith(d1),
        "expected Deg($d2).isParallelWith($d1) to be $expected",
      )
      assertEquals(
        expected,
        Deg(d2).isParallelWith(Deg(d1)),
        "expected Deg($d2).isParallelWith(Deg($d1)) to be $expected",
      )
    }

    t(true, 90, 90)
    t(true, 90, 270)
    t(true, 0, 0)
    t(true, 0, 180)
    t(true, 1, 181)
    t(true, 359, 179)
    t(true, 270, 90)

    t(false, 90, 91)
    t(false, 90, 271)
    t(false, 0, 1)
    t(false, 0, 179)
    t(false, 1, 180)
    t(false, 359, 180)
    t(false, 270, 89)
    t(false, 90, 0)
    t(false, 90, 180)
    t(false, 180, 90)
    t(false, 180, 270)
    t(true, 180, 359.999)
    t(true, 180, 0.0001)
    t(true, 0, 0.0001)
    t(true, 359.9999, 0.0001)
  }

  @Test
  fun testEqualsRelaxed() {
    fun t(expected: Boolean, d1: Number, d2: Number) {
      assertEquals(
        expected,
        Deg(d1).equalsRelaxed(Deg(d2)),
        "expected Deg($d1).equalsRelaxed($d2) to be $expected",
      )
    }

    t(true, 0, 0)
    t(true, 10, 10)
    t(true, 359.999999, 0)
    t(true, 0, 0.001)
    t(true, 0, 0.01)
    t(false, 0, 0.1)
    t(false, 0, 10)
    t(false, 9.9, 10)
  }
}
