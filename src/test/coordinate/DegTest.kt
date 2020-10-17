package test.coordinate

import coordinate.Deg
import coordinate.RotationDirection.Clockwise
import coordinate.RotationDirection.CounterClockwise
import coordinate.RotationDirection.EitherDirection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DegTest {

  @Test
  fun testConstructor() {
    assertEquals(0f, Deg(0).value)
    assertEquals(90f, Deg(90).value)
    assertEquals(350f, Deg(-10).value)
    assertEquals(180f, Deg(-180).value)
    assertEquals(0f, Deg(360).value)
    assertEquals(0f, Deg(-360).value)
    assertEquals(359f, Deg(-361).value)
    assertEquals(1f, Deg(361).value)
    assertEquals(180f, Deg(-900).value)
  }

  @Test
  fun testPlus() {
    assertEquals(0f, (Deg(90) + Deg(-90)).value)
    assertEquals(0f, (Deg(0) + Deg(0)).value)
    assertEquals(10f, (Deg(10) + Deg(0)).value)
    assertEquals(10f, (Deg(0) + Deg(10)).value)
    assertEquals(20f, (Deg(10) + Deg(10)).value)
    assertEquals(180f, (Deg(300) + Deg(600)).value)
  }

  @Test
  fun testMinus() {
    assertEquals(180f, (Deg(90) - Deg(-90)).value)
    assertEquals(0f, (Deg(0) - Deg(0)).value)
    assertEquals(10f, (Deg(10) - Deg(0)).value)
    assertEquals(350f, (Deg(0) - Deg(10)).value)
    assertEquals(0f, (Deg(10) - Deg(10)).value)
    assertEquals(60f, (Deg(300) - Deg(600)).value)
  }

  @Test
  fun testRotation() {
    assertEquals(10f, Deg(90).rotation(Deg(80)))
    assertEquals(90f, Deg(90).rotation(Deg(360)))
    assertEquals(0f, Deg(0).rotation(Deg(0)))
    assertEquals(1f, Deg(0).rotation(Deg(359)))
    assertEquals(45f, Deg(315).rotation(Deg(270)))
    assertEquals(135f, Deg(315).rotation(Deg(90)))
    assertEquals(45f, Deg(270).rotation(Deg(315)))
    assertEquals(135f, Deg(90).rotation(Deg(315)))
  }

  @Test
  fun testRotationEitherDirection() {
    assertEquals(10f, Deg(90).rotation(Deg(80), EitherDirection))
    assertEquals(90f, Deg(90).rotation(Deg(360), EitherDirection))
    assertEquals(0f, Deg(0).rotation(Deg(0), EitherDirection))
    assertEquals(1f, Deg(0).rotation(Deg(359), EitherDirection))
    assertEquals(45f, Deg(315).rotation(Deg(270), EitherDirection))
    assertEquals(135f, Deg(315).rotation(Deg(90), EitherDirection))
    assertEquals(45f, Deg(270).rotation(Deg(315), EitherDirection))
    assertEquals(135f, Deg(90).rotation(Deg(315), EitherDirection))
  }

  @Test
  fun testRotationClockwise() {
    assertEquals(350f, Deg(90).rotation(Deg(80), Clockwise))
    assertEquals(270f, Deg(90).rotation(Deg(360), Clockwise))
    assertEquals(0f, Deg(0).rotation(Deg(0), Clockwise))
    assertEquals(359f, Deg(0).rotation(Deg(359), Clockwise))
    assertEquals(315f, Deg(315).rotation(Deg(270), Clockwise))
    assertEquals(135f, Deg(315).rotation(Deg(90), Clockwise))
    assertEquals(45f, Deg(270).rotation(Deg(315), Clockwise))
    assertEquals(225f, Deg(90).rotation(Deg(315), Clockwise))
  }

  @Test
  fun testRotationCounterClockwise() {
    assertEquals(10f, Deg(90).rotation(Deg(80), CounterClockwise))
    assertEquals(90f, Deg(90).rotation(Deg(360), CounterClockwise))
    assertEquals(0f, Deg(0).rotation(Deg(0), CounterClockwise))
    assertEquals(1f, Deg(0).rotation(Deg(359), CounterClockwise))
    assertEquals(45f, Deg(315).rotation(Deg(270), CounterClockwise))
    assertEquals(225f, Deg(315).rotation(Deg(90), CounterClockwise))
    assertEquals(315f, Deg(270).rotation(Deg(315), CounterClockwise))
    assertEquals(135f, Deg(90).rotation(Deg(315), CounterClockwise))
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
}